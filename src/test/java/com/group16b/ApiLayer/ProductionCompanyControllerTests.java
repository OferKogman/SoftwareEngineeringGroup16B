package com.group16b.ApiLayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.group16b.ApplicationLayer.ProductionCompanyService;
import com.group16b.ApplicationLayer.Objects.Result;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductionCompanyController.class)
public class ProductionCompanyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductionCompanyService productionCompanyService;

    @Test
    void getTotalRevenue_success() throws Exception {

        when(productionCompanyService.displayTotalRevenue("token", 1))
                .thenReturn(Result.makeOk(1500.0));

        mockMvc.perform(
                get("/production-companies/1/total-revenue")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.0"));
    }

    @Test
    void getTotalRevenue_failure() throws Exception {

        when(productionCompanyService.displayTotalRevenue("token", 1))
                .thenReturn(Result.makeFail("not allowed"));

        mockMvc.perform(
                get("/production-companies/1/total-revenue")
                        .header("Authorization", "token"))
                .andExpect(status().isBadRequest());
    }
}
