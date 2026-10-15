```java
package com.pawpal.dao;

import com.pawpal.model.Veterinarian;
import com.pawpal.util.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class VetDAO {

    // View all veterinarians
    public List<Veterinarian> findAll() {

        List<Veterinarian> list = new ArrayList<>();

        String sql = "SELECT u.user_id, u.full_name, u.email, " +
                     "u.password, u.city, v.vet_id, " +
                     "v.specialization, v.license_no " +
                     "FROM vets v " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "ORDER BY v.vet_id";

        System.out.println("1. Starting veterinarian query...");

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                System.out.println("Database connection is null.");
                return list;
            }

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                System.out.println("2. Database query executed.");

                while (rs.next()) {

                    Veterinarian vet = new Veterinarian(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("city"),
                            rs.getInt("vet_id"),
                            rs.getString("specialization"),
                            rs.getString("license_no")
                    );

                    list.add(vet);

                    System.out.println(
                            "Found veterinarian: " + vet.getFullName()
                    );
                }

                System.out.println(
                        "Total veterinarians loaded: " + list.size()
                );
            }

        } catch (SQLException e) {
            System.out.println("Error loading vets: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // Update veterinarian profile
    public boolean update(int vetId, String specialization,
                          String licenseNo) {

        String sql = "UPDATE vets SET specialization = ?, " +
                     "license_no = ? WHERE vet_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, specialization);
            ps.setString(2, licenseNo);
            ps.setInt(3, vetId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating vet: " + e.getMessage());
            return false;
        }
    }

    // Delete veterinarian
    public boolean delete(int vetId) {

        String sql = "DELETE u FROM users u " +
                     "JOIN vets v ON v.user_id = u.user_id " +
                     "WHERE v.vet_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, vetId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting vet: " + e.getMessage());
            return false;
        }
    }
}
```
