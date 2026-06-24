package com.group16b.ApplicationLayer.DTOs.PurchasePolicy;

import com.group16b.ApplicationLayer.Enums.PurchasePolicyTypes;

public abstract class PurchasePolicyDTO {
	private PurchasePolicyTypes type;

	public PurchasePolicyDTO(PurchasePolicyTypes type) {
		this.type = type;
	}

	public PurchasePolicyTypes getType() {
		return type;
	}
}
