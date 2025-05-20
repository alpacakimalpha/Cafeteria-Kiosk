package common;

public record Menu(
        int menuId,
        String name,
        double price,
        int categoryId,
        String description,
        String imagePath,
        boolean soldOut
) {
}
