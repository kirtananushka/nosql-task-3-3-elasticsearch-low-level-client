package com.tananushka.elastic.dto;

import lombok.Data;

@Data
public class EmployeeDTO {
   private String id;
   private String name;
   private String dob;
   private AddressDTO address;
   private String email;
   private String[] skills;
   private int experience;
   private double rating;
   private String description;
   private boolean verified;
   private int salary;

   @Data
   public static class AddressDTO {
      private String country;
      private String town;
   }
}
