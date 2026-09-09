package com.booking.resourcebooking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.booking.resourcebooking.security.JwtService;
import com.booking.resourcebooking.security.CustomUserDetailsService;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.booking.resourcebooking.dto.ResourceRequest;
import com.booking.resourcebooking.dto.ResourceResponse;
import com.booking.resourcebooking.service.ResourceService;

@WebMvcTest(ResourceController.class)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResourceService resourceService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateResourceAsAdmin() throws Exception {

        ResourceResponse response = new ResourceResponse(
                1L,
                "Conference Room",
                "Meeting room",
                new BigDecimal("500.00"),
                true
        );

        when(resourceService.createResource(any(ResourceRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/resources")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Conference Room",
                                    "description": "Meeting room",
                                    "price": 500.00,
                                    "available": true
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Conference Room"));

        verify(resourceService)
                .createResource(any(ResourceRequest.class));
    }


    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserCreatesResource() throws Exception {

        mockMvc.perform(
                post("/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Conference Room",
                                    "description": "Meeting room",
                                    "price": 500.00,
                                    "available": true
                                }
                                """)
        )
        .andExpect(status().isForbidden());

        verify(resourceService, never())
                .createResource(any(ResourceRequest.class));
    }


    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAllResourcesAsUser() throws Exception {

        ResourceResponse response = new ResourceResponse(
                1L,
                "Conference Room",
                "Meeting room",
                new BigDecimal("500.00"),
                true
        );

        Page<ResourceResponse> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );

        when(resourceService.getAllResources(any()))
                .thenReturn(page);

        mockMvc.perform(
                get("/resources")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name")
                .value("Conference Room"));

        verify(resourceService).getAllResources(any());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllResourcesAsAdminWithDescendingSort()
            throws Exception {

        Page<ResourceResponse> page =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 10),
                        0
                );

        when(resourceService.getAllResources(any()))
                .thenReturn(page);

        mockMvc.perform(
                get("/resources")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "price")
                        .param("direction", "desc")
        )
        .andExpect(status().isOk());

        verify(resourceService).getAllResources(any());
    }


    @Test
    @WithMockUser(roles = "USER")
    void shouldGetResourceById() throws Exception {

        ResourceResponse response = new ResourceResponse(
                1L,
                "Conference Room",
                "Meeting room",
                new BigDecimal("500.00"),
                true
        );

        when(resourceService.getResourceById(1L))
                .thenReturn(response);

        mockMvc.perform(
                get("/resources/1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("Conference Room"));

        verify(resourceService).getResourceById(1L);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateResourceAsAdmin() throws Exception {

        ResourceResponse response = new ResourceResponse(
                1L,
                "Updated Room",
                "Updated description",
                new BigDecimal("750.00"),
                true
        );

        when(resourceService.updateResource(
                eq(1L),
                any(ResourceRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
//                put("/resources/1")
//                        .contentType(MediaType.APPLICATION_JSON)
        		put("/resources/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Room",
                                    "description": "Updated description",
                                    "price": 750.00,
                                    "available": true
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("Updated Room"));

        verify(resourceService).updateResource(
                eq(1L),
                any(ResourceRequest.class)
        );
    }


    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserUpdatesResource()
            throws Exception {

        mockMvc.perform(
                put("/resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Room",
                                    "description": "Updated description",
                                    "price": 750.00,
                                    "available": true
                                }
                                """)
        )
        .andExpect(status().isForbidden());

        verify(resourceService, never())
                .updateResource(
                        eq(1L),
                        any(ResourceRequest.class)
                );
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteResourceAsAdmin() throws Exception {

        doNothing()
                .when(resourceService)
                .deleteResource(1L);

        mockMvc.perform(
        		delete("/resources/1")
                .with(csrf())
        )        .andExpect(status().isNoContent());

        verify(resourceService)
                .deleteResource(1L);
    }


    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserDeletesResource()
            throws Exception {

        mockMvc.perform(
                delete("/resources/1")
        )
        .andExpect(status().isForbidden());

        verify(resourceService, never())
                .deleteResource(1L);
    }
}