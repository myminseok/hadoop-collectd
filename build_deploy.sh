git pull origin master
ant
cp build/hadoop-collectd.jar /home/nexr/nexr_platforms/meerkat_backend/hadoop-0.21.0

/home/nexr/nexr_platforms/meerkat_backend/hadoop-0.21.0/bin/stop-all.sh
sleep 1
/home/nexr/nexr_platforms/meerkat_backend/hadoop-0.21.0/bin/start-all.sh

