# VR-Team

## Create a M30 instance in Atlas with 2 Shards

In your Atlas project deploy a new cluster as a sharded cluster, toggle Shard your cluster (M30 and up) to Yes and select 2 shards.

Once the cluster is created, go to collections tab and create a Database store and a Collection products.

## Import Data to store.products Collection

Using the MongoDB shell run mongoimport to import the products.json, available in this project, the store.products Collection.
 ```
mongoimport --host <clustername>-shard-00-00-l5yae.mongodb.net:27016 --ssl --username <USER> --password <PASSWORD> --authenticationDatabase admin --db store --collection products --type json --file products.json 
```	


## Connect to Mongos and Enable Sharding

Using the MongoDB shell connect to one mongos:

```
mongo "mongodb://<clustername>-shard-00-00-l5yae.mongodb.net:27016/test" --ssl --authenticationDatabase admin --username <USER> --password <PASSWORD> 
```	

And then enable sharding:


```
use store

sh.enableSharding("store")

db.products.createIndex({"sku":1})

sh.shardCollection("store.products", { sku: 1 })

sh.status()

```

With the nature of the data the data will be distrubuited by the 2 shards, run:

```
db.products.getShardDistribution()

```

And the result should be:

 ```
Shard RitaDemo-shard-0 at RitaDemo-shard-0/ritademo-shard-00-00-l5yae.mongodb.net:27017,ritademo-shard-00-01-l5yae.mongodb.net:27017,ritademo-shard-00-02-l5yae.mongodb.net:27017
 data : 31.91MiB docs : 217885 chunks : 1
 estimated data per chunk : 31.91MiB
 estimated docs per chunk : 217885

Totals
 data : 107.93MiB docs : 734669 chunks : 3
 Shard RitaDemo-shard-1 contains 70.42% data, 70.34% docs in cluster, avg obj size on shard : 154B
 Shard RitaDemo-shard-0 contains 29.57% data, 29.65% docs in cluster, avg obj size on shard : 153B
  ```
