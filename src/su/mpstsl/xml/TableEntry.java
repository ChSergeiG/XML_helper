package su.mpstsl.xml;


class TableEntry {

    private static final int ENTRY_SIZE = 45;
    static String[] comments = {"код товара Утварь", "код основной родительской категории", "артикул производителя",
            "наименование товара", "заголовок товара", "подзаголовок товара", "цена", "остаток", "дата поступления",
            "страна-производитель", "производитель", "автор", "вес, гр", "вес нетто, гр", "аромат", "длина, см",
            "высота, см", "ширина, см", "размер общий, см", "размер внутренний, см", "форма", "цвет",
            "материал изделия", "техника изготовления", "фактура", "изображение", "украшения", "дополнения",
            "покрытие", "конструкция", "назначение", "комплект", "упаковка", "количество в упаковке",
            "объем, указываются единицы измерения", "состав", "статус", "новинка", "\"подарочность\"",
            "приоритет, чем больше тем важнее", "реализация", "серия"};
    static String[] tags = {"id", "parent", "articul", "name", "title", "subtitle", "price", "quantity", "date_added",
            "country", "manufacturer", "author", "weight", "weight_netto", "flavour", "length", "height", "width",
            "size_general", "size_inner", "form", "color", "material", "technics", "texture", "image", "decorations",
            "additions", "coating", "construction", "purpose", "kit", "packing", "quantity_in_package", "volume",
            "mixture", "status", "novelty", "gift", "priority", "selling", "series"};

    TableEntry() {

        String[] definition = new String[ENTRY_SIZE];
    }



}
