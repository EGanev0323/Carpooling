package com.carpooling.controller;

import com.carpooling.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTrips_withoutAuth_isPermitted() throws Exception {
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk());
    }

    @Test
    void postTrips_withoutAuth_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/trips")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCars_withoutAuth_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealth_withoutAuth_isPermitted() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void postBooking_withoutAuth_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/trips/1/bookings")
                        .contentType("application/json")
                        .content("{\"seatsBooked\":1}"))
                .andExpect(status().isUnauthorized());
    }
}
