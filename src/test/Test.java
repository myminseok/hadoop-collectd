import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.metrics.collectd.CollectdContextConsolidated;

/**
 * hadoop-collectd-types-consolidated.properties에서 context로 조회해서 name이 있으면
 * consolidated로 뿌린다. 없으면 hadoop-collectd-types-consolidated.properties에서
 * 조회한다.개별 전송.
 * 
 */
public class Test {

	private static String PLUGIN = "hadoop";
	private Properties types = new Properties();
	private Map<String, List<String>> typesConsolidated = new Hashtable<String, List<String>>();
	private Map<String, List<Number>> collectdRecordsToSend = new Hashtable<String, List<Number>>();

	public static void main(String... args) throws Exception {
		Test test = new Test();
		// type에 대한 map구축.
		//
		test.types=test.loadTypes("test_hadoop-collectd-types.properties");
		test.typesConsolidated= test.loadTypesConsolidated("test_hadoop-collectd-types-consolidated.properties");
		
		test.initCollectdRecordsToSend();

		test.getType("dfs-FSNamesystem", "");
		test.getType("dfs-FSNamesystem", "BlocksTotal");
		test.getType("dfs-datanode", "");

		test.lookupIndexFromTypeConsolidated("dfs-FSNamesystem", "BlocksTotal");
		test.lookupIndexFromTypeConsolidated("jvm-metrics", "gcTimeMillis--1");
		test.lookupIndexFromTypeConsolidated("jvm-metrics", "gcTimeMillis");

		test.formulateCollectdRecordSet("dfs", "FSNamesystem", "BlocksTotal",10);
		test.formulateCollectdRecordSet("dfs", "FSNamesystem", "BlocksTotals",10);
		test.formulateCollectdRecordSet("dfs", "FSNamesystem", "BlocksTotals",10);
		test.formulateCollectdRecordSet("dfs", "FSNamesystem", "CapacityTotalGB",20);
		test.formulateCollectdRecordSet("dfs", "namenode", "CreateFileOps",20);
		test.formulateCollectdRecordSet("dfs", "FSNamesystem", "CreateFileOpssss",10);
		
	
		test.dispatch();

	}

	private void formulateCollectdRecordSet(String contextName,
			String recordName, String metricName, Number value)
			throws Exception {

		String typedbkey = contextName + "-" + recordName;
		String plugin = Test.PLUGIN + "_" + typedbkey;

		int consolidatedTypeIndex = this.lookupIndexFromTypeConsolidated(typedbkey,
				metricName);

		if (consolidatedTypeIndex >= 0) {

			List<Number> values = this.collectdRecordsToSend
					.get(typedbkey);
			values.set(consolidatedTypeIndex, value);
	
			
		} 
		System.out.println("formulate: typedbkey:" + typedbkey + ", metricName:" + metricName
				+ ",value:"+value+"==> "+typedbkey+"[" + consolidatedTypeIndex + "]==>"+this.collectdRecordsToSend.get(typedbkey));


	}

	
	
	private Properties loadTypes(String file) throws Exception {
		
		InputStream is = getClass().getClassLoader().getResourceAsStream(file);

		Properties typesLocal = new Properties();
		
		if (is != null) {
			try {
				typesLocal.load(is);
			} catch (IOException e) {
				System.out.println("Loading  failed" + file + ": " + e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		} else
			System.out.println("Unable to find: " + file);

		System.out.println("loaded:" + file + ":" + typesLocal);
		return typesLocal;
		

	}

	/**
	 * load 'consolidated.properties" and parse into Hashtable
	 * 
	 * @throws Exception
	 */
	private Map<String, List<String>>  loadTypesConsolidated(String file) throws Exception {
		final Properties propsTypesConsolidated = this.loadTypes(file);
		
		Iterator<Object> keys = propsTypesConsolidated.keySet().iterator();
		
		Map<String, List<String>> typesConsolidatedLocal = new Hashtable<String, List<String>>();
		
		while (keys.hasNext()) {
			String key = keys.next().toString();
			String value = propsTypesConsolidated.getProperty(key);
			String[] typelist = value.split(",");
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < typelist.length; i++) {
				list.add(typelist[i].split(":")[0].trim());
			}

			typesConsolidatedLocal.put(key, list);
		}

		System.out.println("# typesConsolidatedLocal:" + file + ":"
				+ typesConsolidatedLocal);
		return typesConsolidatedLocal;

	}

	private void initCollectdRecordsToSend() throws Exception {

		Iterator<String> keys = typesConsolidated.keySet().iterator();
		while (keys.hasNext()) {
			String tydbkey = keys.next().toString();
	
			List<String> tydbvalues = typesConsolidated.get(tydbkey);
			List<Number> values = new ArrayList<Number>(tydbvalues.size());
			for (int i = 0; i < tydbvalues.size(); i++) {
				values.add(null);
			}
			this.collectdRecordsToSend.put(tydbkey, values);
		}

		System.out.println("collectdRecordsToSend:"
				+ this.collectdRecordsToSend);

	}

	private String getType(String context, String name) {

		String key = context + "-" + name;

		String result = "";
		if (typesConsolidated.containsKey(key)) {
			result = types.getProperty(key);
		}
		System.out.println(key + "====>" + result);
		return result;
	}

	/**
	 * lookup
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	private int lookupIndexFromTypeConsolidated(String typedbKey, String metricName) {

		int indexOfName = -1;
		if (typesConsolidated.containsKey(typedbKey)) {
	
			List<String> result = (List<String>) typesConsolidated.get(typedbKey);
			indexOfName = result.indexOf(metricName);

		}

		return indexOfName;

	}
	
	private void dispatch(){

        Iterator<String> typedbkeys = this.collectdRecordsToSend.keySet().iterator();
        String typedbkey = null;
        while (typedbkeys.hasNext()) {
            typedbkey = typedbkeys.next().toString();
            List<Number> values = this.collectdRecordsToSend.get(typedbkey);

            // if values is empty, not sends.
            boolean dirty = false;
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) != null) {
                    dirty = true;
                    break;
                }
            }
            if (!dirty) {
                System.out.println("skipping dispatch:"+PLUGIN + "_" + typedbkey+":"+values);
                continue;
            }
            System.out.println("dispatching:"+PLUGIN + "_" + typedbkey+":"+values);
        

        }
        

	}

}