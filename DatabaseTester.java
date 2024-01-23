package project;

import java.io.File;

public class DatabaseTester {


private static void testWhere(Parser parser){
        try {
            
            parser.input("select * from mydb");
            parser.parse(); 
            
            parser.input("select * from mydb where surname = 'NULL' group by name");
            parser.parse(); 
            parser.input("select surname, name from mydb where name = 'Marcin'");
            parser.parse();
            parser.input("select name, job, pet " +
                        "from mydb join secdb on mydb.name = secdb.name "+ 
                        "join thirddb on mydb.name = thirddb.name " +
                        "where surname = 'Grawinski'");
            parser.parse();
            parser.input("select * from mydb where id > '3'");
            parser.parse(); 
            parser.input("select id, name from mydb where id < '100'");
            parser.parse(); 
            parser.input("select id, name from mydb where id <= '4'");
            parser.parse(); 
            parser.input("select * from mydb where surname is 'NULL'");
            parser.parse(); 
            
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }


    private static void testSelect(Parser parser){
        try {
            System.out.println("SELECT tests:\n"); 
   
            parser.input("select name from mydb");
            parser.parse();
            parser.input("select * from mydb");
            parser.parse(); 
            parser.input("SELECT surname FROM mydb");
            parser.parse();
            parser.input("select birthday, name from mydb");
            parser.parse();
            parser.input("select * from mydb join secdb on mydb.name = secdb.name");
            parser.parse();
            parser.parse();
            parser.input("select * " +
                        "from mydb join secdb on mydb.name = secdb.name "+ 
                        "join thirddb on mydb.name = thirddb.name ");
            parser.parse();
            parser.input("select * " +
                        "from mydb join secdb on mydb.name = secdb.name ");
            parser.parse();
            testWhere(parser);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    private static void testCreate(Parser parser){
        try {
            File myObj = new File("tmpdb.txt"); 
            myObj.delete();

            System.out.println("CREATE test:\n");
            parser.input("create table tmpdb(name, job, id)");
            parser.parse();
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
    private static void testInsert(Parser parser){
        try{
            System.out.println("INSERT tests:\n");
            parser.input("insert into tmpdb (name, job, id) values ('Anna', 'salesman', '1')");
            parser.parse();
            parser.input("insert into tmpdb (name, job, id) values ('Barbara', 'Smith', '2')");
            parser.parse();
            parser.input("insert into tmpdb (name, job, id) values ('Czarek', 'LLL', '99999')");
            parser.parse();
            parser.input("insert into tmpdb (job) values ('programmer')");
            parser.parse();
            parser.input("select * from tmpdb");
            parser.parse();
            
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
    private static void testUpdate(Parser parser){
        try{
            System.out.println("UPDATE tests:\n");
            parser.input("update tmpdb set name = 'Kacper'");
            parser.parse();
            parser.input("update tmpdb set name = 'Bartosz' where job = 'Smith'");
            parser.parse(); 
            parser.input("update tmpdb set name = 'Anna', id = '7' where id = '1'");
            parser.parse(); 
            parser.input("select * from tmpdb");
            parser.parse();
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
    
    private static void testDelete(Parser parser){
        try{
            System.out.println("DELETE tests:\n");
            parser.input("delete from tmpdb where job = 'Smith'");
            parser.parse();
            parser.input("select * from tmpdb");
            parser.parse();
                        
            parser.input("delete from tmpdb where name = 'Kacper'");
            parser.parse();
            
            parser.input("select * from tmpdb");
            parser.parse();
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
    private static void testErrors(Parser parser){
        try{
            System.out.println("Error tests:\n");
            parser.input("select from mydb");
            parser.parse();
            parser.input("select * mydb");
            parser.parse();
            parser.input("select * from ");
            parser.parse();
            parser.input("select xxx from mydb");
            parser.parse();
            parser.input("select * from mydb where name");
            parser.parse();
            parser.input("select * from mydb where name 'Marcin'");
            parser.parse();
            parser.input("select * from mydb where name = Marcin");
            parser.parse();
            parser.input("select * from mydb where name = Marcin");
            parser.parse();

            parser.input("select * from mydb join secdb name = Marcin");
            parser.parse();
            parser.input("select * from mydb join secdb on name");
            parser.parse();
            parser.input("select * from mydb join secdb on mydb.name = name");
            parser.parse();
            
            parser.input("select * from mydb group name");
            parser.parse();
            parser.input("select * from mydb group by xxx");
            parser.parse();
            
            parser.input("insert mydb");
            parser.parse();
            parser.input("insert into mydb");
            parser.parse();
            parser.input("insert into mydb(name) values");
            parser.parse();
            parser.input("insert into mydb(name) values XXX");
            parser.parse();

            


        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
    

    private static void testUserInput(Parser parser){
        while(parser.userInput()){
            try{                            
                parser.parse();
            }catch(Exception e) {System.out.println("Error" + e); e.printStackTrace();}
        }
    }
   
    public static void main(String[] args) {
        System.out.println("Query");
        Parser parser = new Parser(false);
        testUserInput(parser);
        // testSelect(parser);
        // testCreate(parser);
        // testInsert(parser);
        // testUpdate(parser);
        // testDelete(parser);
        // testErrors(parser);
       
    }
}
