mysql -u root -p

# clear database
DROP DATABASE moviedb;

# create tables
source /home/ubuntu/cs122b-winter19-team-108/project3/sql/createtable.sql;

# insert original data
source /home/ubuntu/cs122b-winter19-team-108/project3/sql/movie-data.sql;

# add quantity attribute into table:sales
alter table sales add quantity int default 0 not null; 

# create table:employees and insert 1 employee
source /home/ubuntu/cs122b-winter19-team-108/project3/sql/createEmployee.sql;
INSERT INTO employees VALUES('classta@email.edu', 'classta', 'TA CS122B');

# load store procedure
exit


# encrypt password
cd /home/ubuntu/2019w-project3-encryption-example/
mvn compile
mvn exec:java -Dexec.mainClass="UpdateSecurePassword"
mvn exec:java -Dexec.mainClass="VerifyPassword"
mvn exec:java -Dexec.mainClass="UpdateSecurePasswordEmp"


cd /home/ubuntu/cs122b-winter19-team-108/
