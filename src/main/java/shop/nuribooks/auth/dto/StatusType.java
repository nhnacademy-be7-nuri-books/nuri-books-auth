package shop.nuribooks.auth.dto;

import java.util.stream.Stream;

public enum StatusType {
	ACTIVE,
	INACTIVE,
	WITHDRAWN;

	public String getValue() {
		return name();
	}

	public static StatusType fromValue(String value) {
		if (value == null) {
			return null;
		}
		return Stream.of(StatusType.values())
			.filter(type -> type.getValue().equalsIgnoreCase(value.substring(7)))
			.findFirst()
			.orElse(null);
	}
}
