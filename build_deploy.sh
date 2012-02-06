#git pull origin master
cd for_hadoop-0.20.2

hadoop_home=/home/nexr/nexr_platforms/hadoop/hadoop
ant
rm -f $hadoop_home/lib/hadoop-collectd.jar
cp -f ./build/hadoop-collectd.jar $hadoop_home/lib/

$hadoop_home/bin/stop-all.sh
sleep 1
$hadoop_home/bin/start-all.sh

