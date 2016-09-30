# Zookeeper Leadership Election through Spring Cloud Cluster

> This implementation uses deprecated classes

Technologies used:
- Spring Cloud
- Zookeeper
- docker, docker-compose

Run two different instances of the application and a Zookeeper node
```
    docker-compose up -d
```

You will see the one application of the two election1, election2 printing out ```Pinging``` to the console.
```
    docker-compose logs -f
```

Try killing one of them and the other should be elected as leader.

```
    docker-compose stop election1
    docker-compose stop election2
```

Try scaling containers like this:
```
    docker-compose scale election1=3
    docker-compose scale election2=3
```

You should always see one instance writing to the log.
