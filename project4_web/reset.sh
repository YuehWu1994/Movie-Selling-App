# reset database
cat sql/resetDB.sql | mysql --database=mysql --password --user=root 


# encrypt password
cd /home/ubuntu/cs122b-winter19-team-108/project4_web/
mvn compile
mvn exec:java -Dexec.mainClass="UpdateSecurePassword"
mvn exec:java -Dexec.mainClass="VerifyPassword"
mvn exec:java -Dexec.mainClass="UpdateSecurePasswordEmp"


# parse xml data
mvn exec:java -Dexec.mainClass="DomParser"


cd /home/ubuntu/cs122b-winter19-team-108/


