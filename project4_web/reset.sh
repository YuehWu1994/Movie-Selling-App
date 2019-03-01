# reset database
cat sql/resetDB.sql | mysql --database=mysql --password --user=root 


# encrypt password
cd /home/ubuntu/2019w-project3-encryption-example/
mvn compile
mvn exec:java -Dexec.mainClass="UpdateSecurePassword"
mvn exec:java -Dexec.mainClass="VerifyPassword"
mvn exec:java -Dexec.mainClass="UpdateSecurePasswordEmp"


cd /home/ubuntu/cs122b-winter19-team-108/
