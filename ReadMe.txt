Run maven-clean to clean the project before staring it if some files have been changed.

Modified OOTB files:


Counter:
Calling counter manually and setting values:
http://localhost:8080/alfresco/service/counter/counter?value=1000

Configured email in alfresco-global.properties

                    
                        
-javaagent:c:\work\spring-components\springloaded-1.2.1.jar -noverify


Deployment preparation:
1. Fix remote settings
2. Fix version
3. Fix redeploy workflows
4. Fix log4j to use common one






Remote debugging
-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044
