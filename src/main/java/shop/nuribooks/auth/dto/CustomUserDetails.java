package shop.nuribooks.auth.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
	private final MemberResponse user;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				return user.role();
			}
		});
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.password();
	}

	@Override
	public String getUsername() {
		return user.username();
	}

	// TODO : 회원 DB에서 해당 계정이 유효한지, 만료되었는지 등 상태 가져와 수정 예정
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
