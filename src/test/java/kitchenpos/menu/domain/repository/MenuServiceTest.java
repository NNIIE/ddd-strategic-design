package kitchenpos.menu.domain.repository;

import kitchenpos.menu.application.service.MenuService;
import kitchenpos.menu.infra.database.menu.InMemoryMenuGroupRepository;
import kitchenpos.menu.infra.database.menu.InMemoryMenuProductRepository;
import kitchenpos.menu.infra.database.menu.InMemoryMenuRepository;
import kitchenpos.menu.infra.database.menu.repository.*;
import kitchenpos.menu.domain.model.Menu;
import kitchenpos.product.domain.repository.InMemoryProductRepository;
import kitchenpos.product.domain.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

class MenuServiceTest {
    private final MenuRepository menuRepository = new InMemoryMenuRepository();
    private final MenuGroupRepository menuGroupRepository = new InMemoryMenuGroupRepository();
    private final MenuProductRepository menuProductRepository = new InMemoryMenuProductRepository();
    private final ProductRepository productRepository = new InMemoryProductRepository();

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService(menuRepository, menuGroupRepository, menuProductRepository, productRepository);
        menuGroupRepository.save(twoChickens());
        productRepository.save(friedChicken());
    }

    @DisplayName("1 개 이상의 등록된 상품으로 메뉴를 등록할 수 있다.")
    @Test
    void create() {
        // given
        final Menu expected = twoFriedChickens();

        // when
        final Menu actual = menuService.create(expected);

        // then
        assertMenu(expected, actual);
    }

    @DisplayName("메뉴의 가격이 올바르지 않으면 등록할 수 없다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"-1000", "33000"})
    void create(final BigDecimal price) {
        // given
        final Menu expected = twoFriedChickens();
        expected.setPrice(price);

        // when
        // then
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> menuService.create(expected));
    }

    @DisplayName("메뉴는 특정 메뉴 그룹에 속해야 한다.")
    @ParameterizedTest
    @NullSource
    void create(final Long menuGroupId) {
        // given
        final Menu expected = twoFriedChickens();
        expected.setMenuGroupId(menuGroupId);

        // when
        // then
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> menuService.create(expected));
    }

    @DisplayName("메뉴의 목록을 조회할 수 있다.")
    @Test
    void list() {
        // given
        final Menu twoFriedChickens = menuRepository.save(twoFriedChickens());

        // when
        final List<Menu> actual = menuService.list();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(Arrays.asList(twoFriedChickens));
    }

    private void assertMenu(final Menu expected, final Menu actual) {
        assertThat(actual).isNotNull();
        assertAll(
                () -> assertThat(actual.getName()).isEqualTo(expected.getName()),
                () -> assertThat(actual.getPrice()).isEqualTo(expected.getPrice()),
                () -> assertThat(actual.getMenuGroupId()).isEqualTo(expected.getMenuGroupId()),
                () -> assertThat(actual.getMenuProducts())
                        .containsExactlyInAnyOrderElementsOf(expected.getMenuProducts())
        );
    }
}