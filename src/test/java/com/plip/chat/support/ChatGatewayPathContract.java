package com.plip.chat.support;

/**
 * Gateway StripPrefix=2 계약.
 * 클라이언트 요청: {@code {gateway}/api/chat{servicePath}} → chat-service {@code {servicePath}}
 */
public final class ChatGatewayPathContract {

	public static final String GATEWAY_PREFIX = "/api/chat";

	public static final String WS_TICKET = "/api/v1/ws/ticket";
	public static final String WS_CHAT = "/ws/chat";

	private ChatGatewayPathContract() {
	}

	public static String toGatewayPath(String servicePath) {
		if (servicePath == null || servicePath.isBlank() || !servicePath.startsWith("/")) {
			throw new IllegalArgumentException("servicePath must start with /");
		}
		return GATEWAY_PREFIX + servicePath;
	}

	public static String toServicePath(String gatewayPath) {
		if (gatewayPath == null || !gatewayPath.startsWith(GATEWAY_PREFIX)) {
			throw new IllegalArgumentException("gatewayPath must start with " + GATEWAY_PREFIX);
		}
		String remainder = gatewayPath.substring(GATEWAY_PREFIX.length());
		return remainder.isEmpty() ? "/" : remainder;
	}
}
