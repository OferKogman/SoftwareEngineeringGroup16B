package com.group16b.ApiLayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.group16b.ApplicationLayer.ProductionCompanyService;
import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.ProductionCompanyDTO;
import com.group16b.ApplicationLayer.Objects.Result;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;

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

    @Test
    void getSalesHistory_success() throws Exception {
        List<OrderDTO> orders = List.of();
        when(productionCompanyService.viewSalesHistory("token", 1))
                .thenReturn(Result.makeOk(orders));
        mockMvc.perform(
                get("/production-companies/1/sales-history")
                        .header("Authorization", "token"))
                .andExpect(status().isOk());
        }

    @Test
    void getSalesHistory_failure() throws Exception {
        when(productionCompanyService.viewSalesHistory("token", 1))
                .thenReturn(Result.makeFail("not allowed"));
        mockMvc.perform(
                get("/production-companies/1/sales-history")
                        .header("Authorization", "token"))
                .andExpect(status().isBadRequest());
        }

    @Test
    void getSalesHistory_exception() throws Exception {
        when(productionCompanyService.viewSalesHistory("token", 1))
                .thenThrow(new RuntimeException("internal error"));

        mockMvc.perform(
                get("/production-companies/1/sales-history")
                        .header("Authorization", "token"))
                .andExpect(status().isInternalServerError());
        }
    
    @Test
    void createProductionCompany_success() throws Exception {

        ProductionCompanyDTO dto = new ProductionCompanyDTO();

        when(productionCompanyService.createProductionCompany(
                "token",
                "New Company Name"))
                .thenReturn(Result.makeOk(dto));

        mockMvc.perform(
                get("/production-companies/1/create-company")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());;
    }

   @Test
    void createProductionCompany_failure() throws Exception {

        when(productionCompanyService.createProductionCompany(
                "token",
                "New Company Name"))
                .thenReturn(Result.makeFail("failed"));

        mockMvc.perform(
                get("/production-companies/1/create-company")
                        .header("Authorization", "token"))
                .andExpect(status().isBadRequest());
        }

    @Test
    void createProductionCompany_notImplemented() throws Exception {

        when(productionCompanyService.createProductionCompany(
                "token",
                "New Company Name"))
                .thenThrow(new UnsupportedOperationException("not implemented"));

        mockMvc.perform(
                get("/production-companies/1/create-company")
                        .header("Authorization", "token"))
                .andExpect(status().isNotImplemented());
        }

    @Test
    void createProductionCompany_exception() throws Exception {

        when(productionCompanyService.createProductionCompany(
                "token",
                "New Company Name"))
                .thenThrow(new RuntimeException("internal error"));

        mockMvc.perform(
                get("/production-companies/1/create-company")
                        .header("Authorization", "token"))
                .andExpect(status().isInternalServerError());
        }
}
