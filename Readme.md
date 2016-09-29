# Redis Leadership Election through Spring Cloud Cluster

Technologies used:
- Spring Cloud
- Redis
- docker, docker-compose

Run two different instances of the application and a Redis node
```
    docker-compose up -d
```

You will see the one application of the two election1, election2 printing out Pinging to the console.
```
    docker-compose logs -f
```

Try killing one of them the other will be elected as leader.

```
    docker-compose stop election1
    docker-compose stop election2
```