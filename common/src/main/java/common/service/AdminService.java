package common.service;

import common.Menu;
import java.util.ArrayList;
import java.util.List;

public class AdminService {
    private final List<Menu> menuList = new ArrayList<>();
    private int nextId = 1;

    public boolean registerMenu(String name, double price, int categoryId, String description, String imagePath, boolean soldOut) {
        Menu menu = new Menu(nextId++, name, price, categoryId, description, imagePath, soldOut);
        menuList.add(menu);
        return true;
    }

    public boolean deleteMenu(int menuId) {
        return menuList.removeIf(menu -> menu.menuId() == menuId);
    }

    public List<Menu> getAllMenus() {
        return new ArrayList<>(menuList);
    }

    public String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1: return "커피";
            case 2: return "차";
            case 3: return "디저트";
            default: return "기타";
        }
    }

    public boolean toggleSoldOut(int menuId) {
        for (int i = 0; i < menuList.size(); i++) {
            Menu menu = menuList.get(i);
            if (menu.menuId() == menuId) {
                Menu updatedMenu = new Menu(
                        menu.menuId(),
                        menu.name(),
                        menu.price(),
                        menu.categoryId(),
                        menu.description(),
                        menu.imagePath(),
                        !menu.soldOut()
                );
                menuList.set(i, updatedMenu);
                return true;
            }
        }
        return false;
    }
}
