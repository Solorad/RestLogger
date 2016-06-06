Create an application solution based on REST to log incoming messages from 3rd party applications.
The solution should handle a simple log message as json and store it for data mining purpose.


### 1. Steps to install and configure any prerequisites for the development environment.
a. Ensure jdk at least version 8 is installed. If not - download and install last version of jdk (http://www.oracle.com/technetwork/java/javase/downloads/index.html).
b. Ensure JAVA_HOME environment variable is set and points to your JDK installation
c. Ensure maven with version higher than 3.2 is installed. If not - download it from https://maven.apache.org/download.cgi. Extract distribution
 archive in any directory. Add the bin directory of extracted maven to the PATH environment variable. The settings of M2 and M2_HOME variables is not necessary.


### 2. Steps to configure the MySql/SqlServer connection.
For development MySQL server was used. So, I used supplemented 'create_crossover.sql' for MySQL.
Its content is in file 'create_crossover_mysql_supplemented.sql'


### 3. Steps to prepare the source code to build properly.
a. Go to project root directory in command line, where is pom.xml.
b. Start it by command 'mvn clean install spring-boot:run'


### 4. Any assumptions made and missing requirements that are not covered in the requirements.
a. Localhost port 8080 is available
b. MySQL is running on localhost
 