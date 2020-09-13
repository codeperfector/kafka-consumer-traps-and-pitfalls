#Intro
Example project to accompany the blog on Kafka consumer traps and pitfalls

Usage

To run the project:
```
cd docker
docker-compose build
./start-containers.sh
```
This will start zookeeper, kafka and application instances.
You can view logs under docker/logs folder.

To stop all containers:
```
cd docker
./stop-containers.sh
```

#WARNING:
The code in this repository is intended to be an example.
Feedback is welcome.
Use this code as you see fit, however I am not responsible for any issues you encounter from using this code.