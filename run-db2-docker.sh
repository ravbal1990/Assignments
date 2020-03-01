docker run -itd --name mydb2 --privileged=true -p 50000:50000 -e LICENSE=accept -e DB2INST1_PASSWORD=admin -e DBNAME=testdb -v ~/db2:/database ibmcom/db2

# username: db2inst1
# password: admin
# database: testdb