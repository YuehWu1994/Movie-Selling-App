######## Install the Flamingo Toolkit on "my" Mac    #############
#### Assume: 
# 1. the toolkit has been downloaded, 
# 2. gcc, make, mysql-server, and libmysqlclient-dev are all installed

# a. change to directory of source code (might be different)
cd  /Users/apple/Downloads/toolkit/src/udf/mysql/ed

# make
make
gcc -Wall -O3 -I/usr/local/opt/mysql-client/include/mysql -shared -o libed.so ed.c
gcc -Wall -O3 -I/usr/local/opt/mysql-client/include/mysql -shared -o libedth.so edth.c
gcc -Wall -O3 -I/usr/local/opt/mysql-client/include/mysql -shared -o libedrec.so edrec.c

# b. copy the link file into mysql plugin directory (You can change this since our mysql versions and locations might be differen)
sudo cp libed*.so /usr/local/mysql/lib/plugin


# c. restart mysql
sudo launchctl unload -F /Library/LaunchDaemons/com.oracle.oss.mysql.mysqld.plist
sudo launchctl load -F /Library/LaunchDaemons/com.oracle.oss.mysql.mysqld.plist 


# d. test 
cat ed.sql edth.sql edrec.sql | mysql --database=mysql --password --user=root 




######## Install the Flamingo Toolkit on "my" AWS instance    #############
#### Assume: 
# the toolkit has been downloaded at your local machine


# a. upload toolkits to AWS from my local machine
scp -i "cs122b_2.pem" ~/Downloads/toolkit_2008-10-14.tgz ubuntu@ec2-13-58-57-112.us-east-2.compute.amazonaws.com:/home/ubuntu

# b. access your AWS
ssh -i "cs122b_2.pem" ubuntu@ec2-13-58-57-112.us-east-2.compute.amazonaws.com 

# c. uncompress toolkit
tar zxvf toolkit_2008-10-14.tgz

# d. install package
sudo apt-get install gcc make mysql-server libmysqlclient-dev

# e. change to directory of source code 
cd toolkit/src/udf/mysql/ed

# f. make
make

# g. copy the link file into mysql plugin directory
sudo cp libed*.so /usr/lib/mysql/plugin/

# h. restart mysql
sudo /etc/init.d/mysql restart

# i. test
cat ed.sql edth.sql edrec.sql | mysql --database=mysql --password --user=root 


