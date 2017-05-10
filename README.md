# Set up AIDA and perform some tests

#### Details of AWS instance:
- instance: m4.xlarge
- username: ubuntu

#### Setting up AIDA
Follow instructions in [AIDA README](https://github.com/yago-naga/aida) in sections _Setting up the Entity Repository_ and _Setting up AIDA_. 

###### PSQL database v9.2 
```
user: kart
host: localhost
port: 5432
pass: 123
db: yago2
```
Gain admin access:
```
sudo -u postgres psql
```
Gain user access:
```
psql -U kart -h localhost -p 5432
```

1. Clone repo

2. Build the dependency JAR (generated in `target/`):
```
mvn package
```

3. Configure `aida.properties` and `database_aida.properties` in `settings/` with database details. 

4. Optionally run `mvn package` again to build JARs to include the settings. Else add settings to classpath everytime. 

5. Compile AidaTest
```
javac -cp "aida/target/aida-3.0.5-SNAPSHOT-jar-with-dependencies.jar:settings/" AidaTest.java
```

6. Run AidaTest
```
java -Xmx12G -cp "aida/target/aida-3.0.5-SNAPSHOT-jar-with-dependencies.jar:settings/" AidaTest
```

`run.sh` does step 5 and 6 for convenience. Pass class name that you wish to run as argument to the script.

`long_input.txt` is a ~3000 word document on the Cuban Missile Crisis that is used as baseline to evaluate performance on long texts. With all speed optimizations off, it takes ~60 mins for AIDA to find 245 mentions. 

Disambiguation accuracy and speed depends upon which Disambiguator is used. Options for Disambiguators can be found [here](https://github.com/yago-naga/aida/tree/master/src/mpi/aida/config/settings/disambiguation). Replace the DisambiguationSettings object with any one of these:
- `CocktailPartyDisambiguationSettings`: No speed optimizations. Takes 30mins for "Michael played for Chelsea.", and 60mins for `long_input.txt`, finding 245 mentions. 
- `FastLocalKeyphraseBasedDisambiguationSettings`: 45s for "Michael played for Chelsea.". 508s for `long_input.txt`, finding 245 entities. 
- `FastCocktailPartyDisambiguationSettings`: 45s for "Michael played for Chelsea.", and 397s for `long_input.txt`, finding 245 entities.  
- PriorOnlyDisambiguationSettings: 45s for "Michael played for Chelsea."

Classes of interest for candidate lookup:
```
- preparation/lookup/EntityLookup.java
	- fillInCandidateEntities()
		Retrieves candidate entities for a mention
		set variable topByPrior as limit candidate limit for each mention (top-k in order of prior probability)
	- getEntitiesForMention()
		Actual worker function, defined as abstract in this class and overloaded in DbLookup.java.
		DBLookup.java calls DataAccess.java
			DataAccess.java calls DataAccessSQL.java
				DataAccessSQL.java queries the DB
- data/Mention.java
```
