# Set up AIDA and perform some tests

#### Details of AWS instance:
- instance: m4.xlarge
- OS image: Ubuntu 16.04
- username: ubuntu

#### Setting up AIDA
Follow instructions in [AIDA README](https://github.com/yago-naga/aida) in sections _Setting up the Entity Repository_ and _Setting up AIDA_. Java 8 and Maven are the only pre-reqs, you may want to install them before setting AIDA up. 

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

1. Clone repo, download the .psql database dump, and run bzcat to import it into PSQL. Detailed instructions about this on the AIDA git repo README. 

2. Build the dependency JAR (generated in `target/`):
```
mvn package
```

3. Configure `aida.properties` and `database_aida.properties` in `settings/` with database details. Configured files can be found in this repo's `settings/`.

4. Run `mvn package` again to build JARs to include the settings. If you modify AIDA code, you will need to package the dependencies again. 

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

Set `topByPrior` inside `fillInCandidateEntities()` to 5, run `mvn package`, and then run `AidaTestFast.java`. This processes `long_input.txt` (3000 word document) in a runtime of 99secs (as opposed to 60mins earlier), finding 245 entities, which mostly looks correct. This sounds like a good trade-off between accuracy and speed. 

Conclusions from tests:
- Text preprocessing and entity extraction time is negligible (<1s for 3000 word doc). 
- The number of candidates for a mention has direct impact on disambiguation algorithm runtime; filtering the DB to reduce number of candidates should help
- Even after filtering the DB, only top-k candidates per mention can be used to get a speedup suited to needs without significant compromise in accuracy.
