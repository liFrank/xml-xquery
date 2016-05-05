# xml-xquery

# Setup Eclipse
Refer to http://stackoverflow.com/questions/30128961/trouble-setting-up-antlr-4-ide-on-eclipse-luna-4-4  
From there, you have to do the following with your version of eclipse  
1. Install XText 2.7.3 (This particular version)  
2. Install ANTLR 4 IDE  

# Import project into Eclipse
1. File->Import...->Existing Projects into Workspace  
2. Select project root  

# ANTLR may complain about a version mismatch when running
1. Project -> Properties
2. ANTLR 4 -> Tool  
2. Enable project specific settings  
3. Add->libs/antlr-4.5.3-complete.jar  
4. Ensure that your added jar is checkmarked  
5. Apply  

## ANTLR generate visitor base classes
This project uses visitors  
1. Project -> Properties  
2. ANTLR 4 -> Tool  
3. Enable project specific settings  
5. Ensure generate parse tree visitors are checkmarked  
6. Apply 

##If you are taking the same classes as ours, please do not copy our codes while we are still working on it. Thank you so much.
