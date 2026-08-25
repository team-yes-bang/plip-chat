package com.plip.chat.application.port.out;

import com.plip.chat.domain.event.MemberReadUpdated;

public interface MemberReadEventPort {

	void publish(MemberReadUpdated event);
}
