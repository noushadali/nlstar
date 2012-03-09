package com.denisk.appengine.nl.shared;

import java.io.Serializable;

public enum UserStatus implements Serializable {
	NOT_LOGGED_IN,
	NOT_ADMIN,
	ADMIN
}
