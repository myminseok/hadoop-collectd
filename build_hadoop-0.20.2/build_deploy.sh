
HADOOP_HOME=/home/nexr/nexr_platforms/hadoop/hadoop

ant
rm -f $HADOOP_HOME/lib/hadoop-collectd*.jar
cp -f ./build/hadoop-collectd-0.20.2.jar $HADOOP_HOME/lib/
cp -f ../lib/commons-vfs-1.0.jar $HADOOP_HOME/lib/
#cp -f ../lib/commons-logging-1.1.1.jar $HADOOP_HOME/lib/

$HADOOP_HOME/bin/stop-all.sh
sleep 1
$HADOOP_HOME/bin/start-all.sh

