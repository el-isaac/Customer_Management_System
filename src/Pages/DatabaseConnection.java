
package Pages;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String url = "jdbc:h2:file:./data/mydb;AUTO_SERVER=TRUE";
    private static final String username = "sa";
    private static final String password = "";

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(url,username,password);
    }

    //this method will create table if they don't exist
    public static void setupDatabase() {
        String createCustomerTable = """
                
                           create table if not exists customer_details (
                               id int auto_increment primary key not null,
                               name varchar(50),
                               last_name varchar(50),
                               street varchar(80),
                               house_number varchar(50),
                               city varchar (80),
                               zip varchar(50),
                               contact varchar(20),
                               email varchar(50)
                           )
                """;
        String createMaterialTable = """
                create table if not exists material(
                id int auto_increment primary key not null,
                name varchar(50),
                type varchar(50),
                price decimal(10,2)
                )
                
                """;
        String createProjectMaterialTable = """
                create table if not exists project_material (
                    id int auto_increment primary key not null,
                    project_id int,
                    material_id int,
                    quantity int,
                    unit_price decimal(10,2),
                    total_price decimal(10,2),
                    
                    constraint  fk_projectmaterial_project
                           foreign key (project_id) references project(project_id) on delete cascade,
                    constraint fk_projectmaterial_material
                            foreign key (material_id) references material(id) on delete set null
                )
                """;
        String createProjectTable = """
                create table if not exists project (
                    project_id int auto_increment primary key not null,
                    customer_id int,
                    project_name varchar(50),
                    project_type varchar(50),
                    project_location varchar(50),
                    start_date date,
                    end_date date,
                    actual_cost decimal(10,2),
                    payment decimal(10,2),
                    
                    constraint fk_project_customer
                        foreign key (customer_id) references customer_details(id)
                        on delete set null
                )
                """;
        try {

            // create tables (does NOT affect existing tables)
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createCustomerTable);
            stmt.executeUpdate(createMaterialTable);
            stmt.executeUpdate(createProjectTable);
            stmt.executeUpdate(createProjectMaterialTable);

            // database migration: add quantity column safely
            addQuantityColumnIntoMaterialTable(stmt);

            System.out.println("Datebase setup complete.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addQuantityColumnIntoMaterialTable(Statement stmt){
        try{
            stmt.executeUpdate("""
                    ALTER TABLE material
                    ADD COLUMN IF NOT EXISTS quantity INT DEFAULT 0
                    """);
        }catch(SQLException e){
            System.out.println("Quantity column already exists in material table.");
        }
    }
}
