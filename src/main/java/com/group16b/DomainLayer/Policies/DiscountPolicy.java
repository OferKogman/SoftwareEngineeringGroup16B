package com.group16b.DomainLayer.Policies;

import java.util.ArrayList;
import java.util.List;

public interface DiscountPolicy {
    double calculateDiscount(double originalPrice);
}
