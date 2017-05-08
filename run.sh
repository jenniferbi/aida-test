javac -cp "../aida/target/aida-3.0.5-SNAPSHOT-jar-with-dependencies.jar:settings/" $1.java
java -Xmx12G -cp "../aida/target/aida-3.0.5-SNAPSHOT-jar-with-dependencies.jar:settings/:." $1
