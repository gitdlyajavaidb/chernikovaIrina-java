import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class practice {

    public static void main(String[] args) {
        try {
            //пр
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:uni;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("");

            task1(ds);
            task2(ds);
            task3(ds);
            task4(ds);

            //дз
            JdbcDataSource hw = new JdbcDataSource();
            hw.setURL("jdbc:h2:mem:hw;DB_CLOSE_DELAY=-1");
            hw.setUser("sa");
            hw.setPassword("");

            initHomeworkTables(hw);
            hw1(hw);
            hw2(hw);
            hw3(hw);
            hw4(hw);
        } catch (SQLException e) {
            System.err.println("ошибка работы с бд: " + e.getMessage());
        }
    }

    //пр

    static void task1(DataSource ds) throws SQLException {
        System.out.println("1 задание");

        String url = "jdbc:h2:mem:uni;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            DatabaseMetaData md = conn.getMetaData();
            System.out.println("drivermanager: " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
        }

        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            System.out.println("datasource: " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
        }
    }

    static void task2(DataSource ds) throws SQLException {
        System.out.println("2 задание");

        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE students (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, group_name VARCHAR(20), gpa DECIMAL(3,2))");
            System.out.println("таблица students создана");
        }

        String insertSql = "INSERT INTO students (name, group_name, gpa) VALUES (?, ?, ?)";
        String[][] data = {
                {"Черникова Ирина", "9-2-ПО-23", "3.95"},
                {"Сидоров Максим", "9-2-ПО-23", "3.40"},
                {"Каримов Артём", "9-2-ПО-23", "2.90"}
        };

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            for (String[] d : data) {
                ps.setString(1, d[0]);
                ps.setString(2, d[1]);
                ps.setBigDecimal(3, new BigDecimal(d[2]));
                int rows = ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    System.out.println("insert: " + d[0] + ", строк=" + rows + ", id=" + keys.getInt(1));
                }
            }
        }
    }

    static void task3(DataSource ds) throws SQLException {
        System.out.println("3 задание");
        printByGroup(ds, "9-2-ПО-23", 3.0);
        printByGroup(ds, "9-2-ПО-23", 0.0);
    }

    static void printByGroup(DataSource ds, String group, double minGpa) throws SQLException {
        System.out.println("группа " + group + ", gpa >= " + minGpa + ":");
        String selectSql = "SELECT id, name, gpa FROM students WHERE group_name = ? AND gpa >= ? ORDER BY gpa DESC";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, group);
            ps.setDouble(2, minGpa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + " " + rs.getString("name") + " " + rs.getBigDecimal("gpa"));
                }
            }
        }
    }

    static void task4(DataSource ds) throws SQLException {
        System.out.println("4 задание");
        String evil = "x' OR '1'='1";

        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students WHERE name = '" + evil + "'");
            rs.next();
            System.out.println("statement + инъекция вернул строк: " + rs.getInt(1));
        }

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM students WHERE name = ?")) {
            ps.setString(1, evil);
            ResultSet rs = ps.executeQuery();
            rs.next();
            System.out.println("preparedstatement вернул строк: " + rs.getInt(1));
        }
    }

    //дз

    static void initHomeworkTables(DataSource ds) throws SQLException {
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE students (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, group_name VARCHAR(20), gpa DECIMAL(3,2))");
            st.execute("CREATE TABLE courses (id INT PRIMARY KEY, title VARCHAR(100), free_seats INT CHECK (free_seats >= 0))");
            st.execute("CREATE TABLE enrollments (student_id INT, course_id INT, PRIMARY KEY (student_id, course_id))");
        }
    }

    static void hw1(DataSource ds) throws SQLException {
        System.out.println("дз 1");
        StudentDao dao = new StudentDao(ds);

        Student a = new Student("Черникова Ирина", "9-2-ПО-23", new BigDecimal("3.95"));
        Student b = new Student("Сидоров Максим", "9-2-ПО-23", new BigDecimal("3.40"));
        Student c = new Student("Каримов Артём", "9-2-ПО-23", new BigDecimal("2.90"));
        dao.save(a);
        dao.save(b);
        dao.save(c);
        System.out.println("save: id = " + a.getId() + ", " + b.getId() + ", " + c.getId());

        System.out.println("findbyid(" + b.getId() + "): " + dao.findById(b.getId()));
        System.out.println("findById(999): " + dao.findById(999));

        c.setGpa(new BigDecimal("3.10"));
        System.out.println("update: " + dao.update(c));
        System.out.println("после update: " + dao.findById(c.getId()));

        System.out.println("findall:");
        List<Student> all = dao.findAll();
        for (Student s : all) {
            System.out.println(s);
        }

        System.out.println("delete(" + c.getId() + "): " + dao.delete(c.getId()));
        System.out.println("после delete осталось: " + dao.findAll().size());
    }

    static void hw2(DataSource ds) throws SQLException {
        System.out.println("дз 2");
        String insertSql = "INSERT INTO students (name, group_name, gpa) VALUES (?, ?, ?)";

        long start1 = System.nanoTime();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (int i = 0; i < 1000; i++) {
                ps.setString(1, "Student" + i);
                ps.setString(2, "BENCH");
                ps.setBigDecimal(3, new BigDecimal("3.00"));
                ps.executeUpdate();
            }
        }
        long time1 = System.nanoTime() - start1;
        System.out.println("циклом: " + time1 / 1000000 + " мс");

        deleteBench(ds);

        long start2 = System.nanoTime();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (int i = 0; i < 1000; i++) {
                ps.setString(1, "Student" + i);
                ps.setString(2, "BENCH");
                ps.setBigDecimal(3, new BigDecimal("3.00"));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        long time2 = System.nanoTime() - start2;
        System.out.println("batch: " + time2 / 1000000 + " мс");

        deleteBench(ds);
    }

    static void deleteBench(DataSource ds) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE group_name = ?")) {
            ps.setString(1, "BENCH");
            ps.executeUpdate();
        }
    }

    static void hw3(DataSource ds) throws SQLException {
        System.out.println("дз 3");

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO courses (id, title, free_seats) VALUES (?, ?, ?)")) {
            ps.setInt(1, 1);
            ps.setString(2, "Java");
            ps.setInt(3, 1);
            ps.executeUpdate();
        }

        try {
            enroll(ds, 1, 1);
            System.out.println("студент 1 записан, мест: " + freeSeats(ds, 1));
        } catch (SQLException e) {
            System.out.println("студент 1 не записан: " + e.getMessage());
        }

        try {
            enroll(ds, 2, 1);
            System.out.println("студент 2 записан, мест: " + freeSeats(ds, 1));
        } catch (SQLException e) {
            System.out.println("студент 2 не записан (rollback): " + e.getMessage());
            System.out.println("мест: " + freeSeats(ds, 1) + ", записей: " + enrollmentsCount(ds));
        }
    }

    static void enroll(DataSource ds, int studentId, int courseId) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)");
                 PreparedStatement chk = conn.prepareStatement("SELECT free_seats FROM courses WHERE id = ?");
                 PreparedStatement upd = conn.prepareStatement("UPDATE courses SET free_seats = free_seats - 1 WHERE id = ?")) {
                ins.setInt(1, studentId);
                ins.setInt(2, courseId);
                ins.executeUpdate();

                chk.setInt(1, courseId);
                try (ResultSet rs = chk.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) <= 0) {
                        throw new SQLException("нет свободных мест");
                    }
                }

                upd.setInt(1, courseId);
                upd.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    static int freeSeats(DataSource ds, int courseId) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT free_seats FROM courses WHERE id = ?")) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    static int enrollmentsCount(DataSource ds) throws SQLException {
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM enrollments")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public static double avgGpaByGroup(Connection conn, String grp) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT AVG(gpa) FROM students WHERE group_name = ?")) {
            ps.setString(1, grp);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    static void hw4(DataSource ds) throws SQLException {
        System.out.println("дз 4");
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE ALIAS AVG_GPA_BY_GROUP FOR \"practice.avgGpaByGroup\"");
        }
        try (Connection conn = ds.getConnection();
             CallableStatement cs = conn.prepareCall("{? = call AVG_GPA_BY_GROUP(?)}")) {
            cs.registerOutParameter(1, Types.DOUBLE);
            cs.setString(2, "9-2-ПО-23");
            cs.execute();
            System.out.println("средний балл группы 9-2-ПО-23 = " + cs.getDouble(1));
        }
    }
}

class Student {
    private int id;
    private String name;
    private String group;
    private BigDecimal gpa;

    public Student(String name, String group, BigDecimal gpa) {
        this.name = name;
        this.group = group;
        this.gpa = gpa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getGroup() { return group; }
    public BigDecimal getGpa() { return gpa; }
    public void setGpa(BigDecimal gpa) { this.gpa = gpa; }

    public String toString() {
        return id + " " + name + " " + group + " " + gpa;
    }
}

class StudentDao {
    private DataSource ds;

    public StudentDao(DataSource ds) {
        this.ds = ds;
    }

    public void save(Student s) throws SQLException {
        String sql = "INSERT INTO students (name, group_name, gpa) VALUES (?, ?, ?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getGroup());
            ps.setBigDecimal(3, s.getGpa());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                s.setId(keys.getInt(1));
            }
        }
    }

    public Student findById(int id) throws SQLException {
        String sql = "SELECT id, name, group_name, gpa FROM students WHERE id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student(rs.getString("name"), rs.getString("group_name"), rs.getBigDecimal("gpa"));
                    s.setId(rs.getInt("id"));
                    return s;
                }
                return null;
            }
        }
    }

    public List<Student> findAll() throws SQLException {
        List<Student> result = new ArrayList<>();
        String sql = "SELECT id, name, group_name, gpa FROM students ORDER BY id";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student s = new Student(rs.getString("name"), rs.getString("group_name"), rs.getBigDecimal("gpa"));
                s.setId(rs.getInt("id"));
                result.add(s);
            }
        }
        return result;
    }

    public boolean update(Student s) throws SQLException {
        String sql = "UPDATE students SET name = ?, group_name = ?, gpa = ? WHERE id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getGroup());
            ps.setBigDecimal(3, s.getGpa());
            ps.setInt(4, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}