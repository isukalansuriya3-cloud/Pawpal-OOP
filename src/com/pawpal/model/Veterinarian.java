
package com.pawpal.model;

public class Veterinarian extends User {

    private int vetId;
    private String specialization;
    private String licenseNo;

    public Veterinarian(int userId, String fullName, String email,
                        String password, String city,
                        int vetId, String specialization, String licenseNo) {

        super(userId, fullName, email, password, city);

        this.vetId = vetId;
        this.specialization = specialization;
        this.licenseNo = licenseNo;
    }

    public int getVetId() {
        return vetId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    @Override
    public String getRole() {
        return "VET";
    }

    @Override
    public String getDashboardTitle() {
        return "Veterinarian Dashboard";
    }

    @Override
    public String getAppointmentScopeSql() {
        return "WHERE v.user_id = " + getUserId();
    }

    @Override
    public boolean canBookAppointments() {
        return false;
}        
    public void setSpecialization(String specialization) {
    this.specialization = specialization;
}
    public void setLicenseNo(String licenseNo) {
    this.licenseNo = licenseNo;    
    }
}
