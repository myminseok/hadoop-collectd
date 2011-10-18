/*
 * Copyright 2009 Hyperic, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.metrics.collectd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.metrics.ContextFactory;
import org.apache.hadoop.metrics.MetricsException;
import org.apache.hadoop.metrics.spi.AbstractMetricsContext;
import org.apache.hadoop.metrics.spi.OutputRecord;
import org.apache.hadoop.metrics.spi.Util;
import org.collectd.protocol.Network;
import org.collectd.protocol.UdpSender;
import org.collectd.api.ValueList;

/**
 * modified from CollectdContext.java Context for sending metrics to collectd.
 */
public class CollectdContextConsolidated extends AbstractMetricsContext {
    private static final Log LOG = LogFactory
            .getLog(CollectdContextConsolidated.class);

    static final String PLUGIN = "hadoop";
    private static final String PERIOD_PROPERTY = "period";
    private static final String SERVERS_PROPERTY = "servers";
    private static final String DEFAULT_TYPE = "gauge";

    private String instance;
    private UdpSender sender;
    private Properties types = new Properties();
    private Map<String, List<String>> typesConsolidated = new Hashtable<String, List<String>>();

    private Map<String, List<Number>> collectdRecordsToSend = new Hashtable<String, List<Number>>();

    private Properties loadTypes(String file) throws Exception {

        InputStream is = getClass().getClassLoader().getResourceAsStream(file);

        Properties typesLocal = new Properties();

        if (is != null) {
            try {
                typesLocal.load(is);
            } catch (IOException e) {
                LOG.error("Loading  failed" + file + ": " + e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } else {
            LOG.warn("Unable to find: " + file);
        }
        LOG.info("loaded:" + file + ":" + typesLocal);
        return typesLocal;

    }

    /**
     * load 'consolidated.properties" and parse into Hashtable
     * 
     * @throws Exception
     */
    private Map<String, List<String>> loadTypesConsolidated(String file)
            throws Exception {
        final Properties propsTypesConsolidated = this.loadTypes(file);

        Iterator<Object> keys = propsTypesConsolidated.keySet().iterator();

        Map<String, List<String>> typesConsolidatedLocal = new Hashtable<String, List<String>>();

        while (keys.hasNext()) {
            String key = keys.next().toString();
            String value = propsTypesConsolidated.getProperty(key);
            String[] typelist = value.split(",");
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < typelist.length; i++) {
                list.add(typelist[i].trim());
            }

            typesConsolidatedLocal.put(key, list);
        }

        LOG.info("# typesConsolidatedLocal:" + file + ":"
                + typesConsolidatedLocal);
        return typesConsolidatedLocal;

    }

//    private void initCollectdRecordsToSend() throws Exception {
//
//        Iterator<String> keys = typesConsolidated.keySet().iterator();
//        while (keys.hasNext()) {
//            String tydbkey = keys.next().toString();
//
//            List<String> tydbvalues = typesConsolidated.get(tydbkey);
//            List<Number> values = new ArrayList<Number>(tydbvalues.size());
//            for (int i = 0; i < tydbvalues.size(); i++) {
//                values.add(null);
//            }
//            this.collectdRecordsToSend.put(tydbkey, values);
//        }
//
//    }
    
    private List<Number> initSingleCollectdRecordToSend(String typedbkey ){
        List<String> tydbvalues = typesConsolidated.get(typedbkey);
        List<Number> values = new ArrayList<Number>(tydbvalues.size());
        for (int i = 0; i < tydbvalues.size(); i++) {
            values.add(null);
        }
        this.collectdRecordsToSend.put(typedbkey, values);
        return values;
        
    }

    public void init(String contextName, ContextFactory factory) {
        super.init(contextName, factory);

        String periodStr = getAttribute(PERIOD_PROPERTY);
        if (periodStr != null) {
            int period = 0;
            try {
                period = Integer.parseInt(periodStr);
            } catch (NumberFormatException nfe) {
            }
            if (period <= 0) {
                throw new MetricsException("Invalid period: " + periodStr);
            }
            setPeriod(period);

            try {
                this.types = this.loadTypes("hadoop-collectd-types.properties");
                this.typesConsolidated = this
                        .loadTypesConsolidated("hadoop-collectd-types-consolidated.properties");

            } catch (Exception e) {
                StackTraceElement[] elem = e.getStackTrace();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < elem.length; i++)
                    sb.append(elem[i] + "\n");

                LOG.error("Loading  failed : " + e + " " + sb.toString());

            }

        }

        sender = new UdpSender();
        List<InetSocketAddress> metricsServers = Util.parse(
                getAttribute(SERVERS_PROPERTY), Network.DEFAULT_PORT);

        for (InetSocketAddress addr : metricsServers) {
            sender.addServer(addr);
        }

        instance = defaultInstance();
    }

    private String defaultInstance() {
        // -Dhadoop.log.file=logs/hadoop-user-tasktracker-hostname.out
        String name = System.getProperty("hadoop.log.file");
        if (name == null) {
            return null;
        }
        name = new File(name).getName();
        String[] parts = name.split("-");
        if (parts.length >= 3) {
            return parts[2]; // tasktracker
        }
        return null;
    }

    /**
     * @param contextName dfs, jvm, rpc, mapred
     * @param recordName FSNamesystem, namenode, datanode, metrics, jobtracker, datatracker, shuffleOutput
     * 
     */
    protected void emitRecord(String contextName, String recordName,
            OutputRecord outRec) throws IOException {

        String context = contextName + "-" + recordName; // dfs-FSNamesystem
        String typedbkey = contextName + "_" + recordName; // dfs_FSNamesystem
        String plugin = PLUGIN + "_" + context; // hadoop_dfs-FSNamesystem

        try {
            //this.initCollectdRecordsToSend();
            for (String metricName : outRec.getMetricNames()) {
                Number value = outRec.getMetric(metricName);
                if (!this.accumulateAsConsolidated(typedbkey, contextName,
                        recordName, metricName, value)) {
                    this.emitAsSingle(plugin, typedbkey, metricName, value);
                }
            }
        } catch (Exception e) {
            StackTraceElement[] elem = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < elem.length; i++)
                sb.append(elem[i] + "\n");

            LOG.error("single Record failed : " + e + " " + sb.toString());
        }
        
        try {
            this.dispatchConsolidated(plugin);
        } catch (Exception e) {
            StackTraceElement[] elem = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < elem.length; i++)
                sb.append(elem[i] + "\n");

            LOG.error("consolidated Record  failed : " + e + " "
                    + sb.toString());
        }
    }

    private boolean accumulateAsConsolidated(String typedbkey,
            String contextName, String recordName, String metricName,
            Number value) {
       
        int consolidatedTypeIndex = this.lookupIndexFromTypeConsolidated(
                typedbkey, metricName);

        if (consolidatedTypeIndex >= 0) {
            List<Number> values = this.collectdRecordsToSend.get(typedbkey);
            if (values==null || values.isEmpty()){
               values= this.initSingleCollectdRecordToSend(typedbkey);
            }
            values.set(consolidatedTypeIndex, value);
        }
        return (consolidatedTypeIndex >= 0) ? true : false;
    }

    /**
     * 
     * @param typedbKey
     * @param metricName
     * @return
     */
    private int lookupIndexFromTypeConsolidated(String typedbKey,
            String metricName) {

        int indexOfName = -1;
        if (this.typesConsolidated.containsKey(typedbKey)) {

            List<String> result = (List<String>) this.typesConsolidated
                    .get(typedbKey);
            indexOfName = result.indexOf(metricName);

        }

        return indexOfName;

    }

    private void emitAsSingle(String plugin, String typedbkey,
            String metricName, Number value) {

        String type = getType(typedbkey, metricName);
        if (type.equals("NONE")) {
            return; // consider disabled
        }

        emitMetric(plugin, metricName, type, value);
    }

    private String getType(String context, String name) {
        return types.getProperty(context + "-" + name, DEFAULT_TYPE);
    }

    private void emitMetric(String plugin, String name, String type,
            Number value) {
        ValueList vl = new ValueList();

        vl.setTime(System.currentTimeMillis());
        vl.setInterval(getPeriod());
        vl.setPlugin(plugin);
        vl.setPluginInstance(instance);
        vl.setType(type);
        vl.setTypeInstance(name);
        vl.addValue(value);
        sender.dispatch(vl);
        LOG.info("emitMetric : sent ==>" + vl);
    }

    private void dispatchConsolidated(String plugin) throws Exception {
        ValueList vl = new ValueList();

        vl.setTime(System.currentTimeMillis());
        vl.setInterval(getPeriod());
        vl.setPlugin(plugin);
        vl.setPluginInstance(instance);// namenode, secondarynamenode, datanode, jobtracker.
                                       // tasktracker.

        Iterator<String> typedbkeys = this.collectdRecordsToSend.keySet()
                .iterator();
        String typedbkey = null;
        while (typedbkeys.hasNext()) {
            typedbkey = typedbkeys.next().toString();
            List<Number> values = this.collectdRecordsToSend.get(typedbkey);

            // if values is empty, not sends.
            if(values==null || values.isEmpty())
                continue;
            
            boolean includeNull = false;
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) == null) {
                    includeNull = true;
                    break;
                }
            }
            if (includeNull) {
                LOG.warn(" invalid(null) values found, skipping, plugin:" + plugin
                        + ",typedbkey:" + typedbkey + ", values:" + values );
                continue;
            }

            vl.setType(typedbkey);
            vl.setTypeInstance("");
            vl.setValues(values);
            try {
                sender.dispatch(vl);
                LOG.debug("sending SUCCESS: typedbkey:" + typedbkey
                        + ",vl:" + vl);
            } catch (Exception e) {
                StackTraceElement[] elem = e.getStackTrace();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < elem.length; i++)
                    sb.append(elem[i] + "\n");

                LOG.error("sending consolidated record failed:"+ e + ":plugin:" + plugin
                        + ", typedbkey:" + typedbkey + ", vl:" + vl + ", trace:" + sb.toString());
            }

            vl.clearValues();

        }
    }
}
