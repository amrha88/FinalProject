package com.example.automate.domain.model

import java.util.Calendar

data class CarModel(
    val name: String,
    val startYear: Int,
    val endYear: Int? = null
)

object CarCatalog {

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val modelsByManufacturer: Map<String, List<CarModel>> = linkedMapOf(
        "Abarth" to listOf(
            CarModel("500", 2008, 2024),
            CarModel("595", 2013, 2024),
            CarModel("695", 2010, 2024),
            CarModel("124 Spider", 2016, 2019),
            CarModel("Grande Punto", 2007, 2018),
            CarModel("600e", 2024)
        ),
        "Acura" to listOf(
            CarModel("Integra", 2023),
            CarModel("MDX", 2001),
            CarModel("RDX", 2006),
            CarModel("TLX", 2014),
            CarModel("ILX", 2012, 2022),
            CarModel("NSX", 2016, 2022)
        ),
        "Alfa Romeo" to listOf(
            CarModel("Giulia", 2016),
            CarModel("Stelvio", 2017),
            CarModel("Tonale", 2022),
            CarModel("Giulietta", 2010, 2020),
            CarModel("MiTo", 2008, 2018),
            CarModel("4C", 2013, 2020)
        ),
        "Aston Martin" to listOf(
            CarModel("DB11", 2016, 2023),
            CarModel("DB12", 2023),
            CarModel("Vantage", 2018),
            CarModel("DBX", 2020),
            CarModel("DBS Superleggera", 2018),
            CarModel("Vanquish", 2024)
        ),
        "Audi" to listOf(
            CarModel("A1", 2010),
            CarModel("A3", 1996),
            CarModel("A4", 1994),
            CarModel("A6", 1994),
            CarModel("Q3", 2011),
            CarModel("Q5", 2008),
            CarModel("Q7", 2005),
            CarModel("TT", 1998, 2023),
            CarModel("e-tron", 2018)
        ),
        "BAIC" to listOf(
            CarModel("BJ40", 2013),
            CarModel("X55", 2019),
            CarModel("X7", 2020),
            CarModel("EU5", 2018),
            CarModel("Beijing X3", 2018)
        ),
        "Bentley" to listOf(
            CarModel("Continental GT", 2003),
            CarModel("Flying Spur", 2005),
            CarModel("Bentayga", 2015),
            CarModel("Mulsanne", 2010, 2020)
        ),
        "BMW" to listOf(
            CarModel("1 Series", 2004),
            CarModel("2 Series", 2014),
            CarModel("3 Series", 1975),
            CarModel("5 Series", 1972),
            CarModel("7 Series", 1977),
            CarModel("X1", 2009),
            CarModel("X3", 2003),
            CarModel("X5", 1999),
            CarModel("iX", 2021)
        ),
        "Bugatti" to listOf(
            CarModel("Veyron", 2005, 2015),
            CarModel("Chiron", 2016, 2024),
            CarModel("Divo", 2018, 2021),
            CarModel("Tourbillon", 2024)
        ),
        "Buick" to listOf(
            CarModel("Enclave", 2008),
            CarModel("Encore", 2013),
            CarModel("Envision", 2015),
            CarModel("LaCrosse", 2005, 2019),
            CarModel("Regal", 1997, 2020)
        ),
        "BYD" to listOf(
            CarModel("Seal", 2023),
            CarModel("Han", 2020),
            CarModel("Tang", 2018),
            CarModel("Song Plus", 2012),
            CarModel("Qin", 2013),
            CarModel("Dolphin", 2021),
            CarModel("Atto 3", 2022)
        ),
        "Cadillac" to listOf(
            CarModel("Escalade", 1999),
            CarModel("CT4", 2019),
            CarModel("CT5", 2019),
            CarModel("XT4", 2018),
            CarModel("XT5", 2016),
            CarModel("Lyriq", 2022)
        ),
        "Chery" to listOf(
            CarModel("Tiggo 4", 2017),
            CarModel("Tiggo 7", 2016),
            CarModel("Tiggo 8", 2018),
            CarModel("Tiggo 9", 2024),
            CarModel("Arrizo 8", 2022)
        ),
        "Chevrolet" to listOf(
            CarModel("Spark", 2009),
            CarModel("Malibu", 1997),
            CarModel("Equinox", 2004),
            CarModel("Camaro", 2009, 2024),
            CarModel("Corvette", 1953),
            CarModel("Tahoe", 1994),
            CarModel("Silverado", 1998)
        ),
        "Chrysler" to listOf(
            CarModel("300", 2004),
            CarModel("Pacifica", 2016),
            CarModel("Voyager", 2019)
        ),
        "Citroen" to listOf(
            CarModel("C1", 2005, 2022),
            CarModel("C3", 2002),
            CarModel("C4", 2004),
            CarModel("C5 Aircross", 2018),
            CarModel("Berlingo", 1996)
        ),
        "Cupra" to listOf(
            CarModel("Formentor", 2020),
            CarModel("Leon", 2018),
            CarModel("Ateca", 2018),
            CarModel("Born", 2021),
            CarModel("Tavascan", 2024)
        ),
        "Dacia" to listOf(
            CarModel("Sandero", 2004),
            CarModel("Duster", 2010),
            CarModel("Logan", 2004),
            CarModel("Spring", 2021),
            CarModel("Jogger", 2022)
        ),
        "Daihatsu" to listOf(
            CarModel("Terios", 1997),
            CarModel("Sirion", 1998, 2019),
            CarModel("Copen", 2002),
            CarModel("Ayla", 2013)
        ),
        "Dodge" to listOf(
            CarModel("Charger", 2024),
            CarModel("Challenger", 2008, 2023),
            CarModel("Durango", 2011),
            CarModel("Journey", 2009, 2020)
        ),
        "DS Automobiles" to listOf(
            CarModel("DS 3", 2015),
            CarModel("DS 4", 2011),
            CarModel("DS 7", 2017),
            CarModel("DS 9", 2020)
        ),
        "Ferrari" to listOf(
            CarModel("488", 2015, 2019),
            CarModel("F8 Tributo", 2019, 2023),
            CarModel("Roma", 2019),
            CarModel("Portofino", 2017, 2023),
            CarModel("SF90 Stradale", 2019),
            CarModel("812 Superfast", 2017, 2022),
            CarModel("296 GTB", 2021),
            CarModel("Purosangue", 2022)
        ),
        "Fiat" to listOf(
            CarModel("500", 2007),
            CarModel("Panda", 1980),
            CarModel("Tipo", 2015),
            CarModel("Punto", 1993, 2018),
            CarModel("500X", 2014)
        ),
        "Ford" to listOf(
            CarModel("Fiesta", 1976, 2023),
            CarModel("Focus", 1998, 2025),
            CarModel("Mustang", 1964),
            CarModel("F-150", 1975),
            CarModel("Explorer", 1990),
            CarModel("Kuga", 2000),
            CarModel("Puma", 2019),
            CarModel("Ranger", 1983)
        ),
        "Genesis" to listOf(
            CarModel("G70", 2017),
            CarModel("G80", 2016),
            CarModel("G90", 2016),
            CarModel("GV60", 2021),
            CarModel("GV70", 2020),
            CarModel("GV80", 2020)
        ),
        "Geely" to listOf(
            CarModel("Coolray", 2018),
            CarModel("Emgrand", 2009),
            CarModel("Boyue", 2016),
            CarModel("Monjaro", 2021)
        ),
        "GMC" to listOf(
            CarModel("Sierra", 1987),
            CarModel("Yukon", 1991),
            CarModel("Acadia", 2006),
            CarModel("Terrain", 2009),
            CarModel("Canyon", 2003)
        ),
        "Great Wall" to listOf(
            CarModel("Haval H6", 2011),
            CarModel("Haval Jolion", 2020),
            CarModel("Haval H9", 2014),
            CarModel("Tank 300", 2020),
            CarModel("Ora Funky Cat", 2022)
        ),
        "Honda" to listOf(
            CarModel("Jazz", 2001),
            CarModel("Civic", 1972),
            CarModel("Accord", 1976),
            CarModel("CR-V", 1995),
            CarModel("HR-V", 2021),
            CarModel("Pilot", 2002)
        ),
        "Hyundai" to listOf(
            CarModel("i10", 2007),
            CarModel("i20", 2008),
            CarModel("Elantra", 1990),
            CarModel("Tucson", 2004),
            CarModel("Santa Fe", 2000),
            CarModel("Kona", 2017),
            CarModel("Ioniq 5", 2021)
        ),
        "Infiniti" to listOf(
            CarModel("Q50", 2013),
            CarModel("Q60", 2016),
            CarModel("QX50", 2013),
            CarModel("QX60", 2012),
            CarModel("QX80", 2004)
        ),
        "Isuzu" to listOf(
            CarModel("D-Max", 2002),
            CarModel("MU-X", 2013)
        ),
        "Jaguar" to listOf(
            CarModel("XE", 2015, 2024),
            CarModel("XF", 2007),
            CarModel("F-Type", 2013, 2024),
            CarModel("F-Pace", 2016),
            CarModel("E-Pace", 2017),
            CarModel("I-Pace", 2018, 2024)
        ),
        "Jeep" to listOf(
            CarModel("Renegade", 2014),
            CarModel("Compass", 2016),
            CarModel("Cherokee", 2013, 2023),
            CarModel("Grand Cherokee", 1992),
            CarModel("Wrangler", 1986),
            CarModel("Avenger", 2023)
        ),
        "Kia" to listOf(
            CarModel("Picanto", 2004),
            CarModel("Rio", 1999),
            CarModel("Ceed", 2006),
            CarModel("Sportage", 1993),
            CarModel("Sorento", 2002),
            CarModel("Telluride", 2019),
            CarModel("EV6", 2021)
        ),
        "Koenigsegg" to listOf(
            CarModel("CC8S", 2002, 2004),
            CarModel("CCX", 2006, 2010),
            CarModel("Agera", 2010, 2018),
            CarModel("Regera", 2015),
            CarModel("Jesko", 2019),
            CarModel("Gemera", 2020)
        ),
        "Lamborghini" to listOf(
            CarModel("Gallardo", 2003, 2013),
            CarModel("Huracan", 2014, 2024),
            CarModel("Aventador", 2011, 2022),
            CarModel("Urus", 2018),
            CarModel("Revuelto", 2023)
        ),
        "Lancia" to listOf(
            CarModel("Ypsilon", 1995),
            CarModel("Delta", 2008, 2014)
        ),
        "Land Rover" to listOf(
            CarModel("Defender", 2020),
            CarModel("Discovery Sport", 2014),
            CarModel("Discovery", 1989),
            CarModel("Range Rover Evoque", 2011),
            CarModel("Range Rover Velar", 2017),
            CarModel("Range Rover Sport", 2005),
            CarModel("Range Rover", 1970)
        ),
        "Lexus" to listOf(
            CarModel("IS", 1998),
            CarModel("ES", 1989),
            CarModel("NX", 2014),
            CarModel("UX", 2018),
            CarModel("RX", 1998),
            CarModel("LS", 1989),
            CarModel("LX", 1996)
        ),
        "Lincoln" to listOf(
            CarModel("Corsair", 2019),
            CarModel("Nautilus", 2018),
            CarModel("Aviator", 2019),
            CarModel("Navigator", 1997)
        ),
        "Maserati" to listOf(
            CarModel("Ghibli", 2013),
            CarModel("Levante", 2016),
            CarModel("Quattroporte", 2013),
            CarModel("Grecale", 2022),
            CarModel("GranTurismo", 2023),
            CarModel("MC20", 2020)
        ),
        "Mazda" to listOf(
            CarModel("Mazda2", 2007),
            CarModel("Mazda3", 2003),
            CarModel("Mazda6", 2002),
            CarModel("CX-30", 2019),
            CarModel("CX-5", 2012),
            CarModel("CX-90", 2023),
            CarModel("MX-5 Miata", 1989)
        ),
        "McLaren" to listOf(
            CarModel("570S", 2015, 2019),
            CarModel("720S", 2017, 2024),
            CarModel("GT", 2019, 2023),
            CarModel("Artura", 2021),
            CarModel("750S", 2023)
        ),
        "Mercedes-Benz" to listOf(
            CarModel("A-Class", 1997),
            CarModel("C-Class", 1993),
            CarModel("E-Class", 1993),
            CarModel("S-Class", 1972),
            CarModel("G-Class", 1979),
            CarModel("GLA", 2013),
            CarModel("GLC", 2015),
            CarModel("GLE", 2015),
            CarModel("EQS", 2021)
        ),
        "MG" to listOf(
            CarModel("MG3", 2011),
            CarModel("MG4", 2022),
            CarModel("MG5", 2020),
            CarModel("MG HS", 2018),
            CarModel("MG ZS", 2017),
            CarModel("Cyberster", 2024)
        ),
        "Mini" to listOf(
            CarModel("Cooper", 2001),
            CarModel("Clubman", 2007, 2024),
            CarModel("Countryman", 2010),
            CarModel("Convertible", 2004, 2024)
        ),
        "Mitsubishi" to listOf(
            CarModel("Mirage", 2012),
            CarModel("ASX", 2010),
            CarModel("Eclipse Cross", 2017),
            CarModel("Outlander", 2001),
            CarModel("Pajero", 1982, 2021),
            CarModel("Lancer", 1973, 2017)
        ),
        "Nissan" to listOf(
            CarModel("Micra", 1982, 2024),
            CarModel("Juke", 2010),
            CarModel("Qashqai", 2006),
            CarModel("X-Trail", 2000),
            CarModel("Leaf", 2010),
            CarModel("Navara", 1997),
            CarModel("Altima", 1992, 2025)
        ),
        "NIO" to listOf(
            CarModel("ES6", 2018),
            CarModel("ES8", 2018),
            CarModel("EC6", 2019),
            CarModel("ET5", 2022),
            CarModel("ET7", 2022)
        ),
        "Opel" to listOf(
            CarModel("Corsa", 1982),
            CarModel("Astra", 1991),
            CarModel("Insignia", 2008, 2022),
            CarModel("Mokka", 2012),
            CarModel("Crossland", 2017),
            CarModel("Grandland", 2017)
        ),
        "Pagani" to listOf(
            CarModel("Zonda", 1999, 2018),
            CarModel("Huayra", 2011),
            CarModel("Utopia", 2022)
        ),
        "Peugeot" to listOf(
            CarModel("108", 2014, 2021),
            CarModel("208", 2012),
            CarModel("2008", 2013),
            CarModel("308", 2007),
            CarModel("3008", 2009),
            CarModel("508", 2010),
            CarModel("5008", 2009)
        ),
        "Porsche" to listOf(
            CarModel("911", 1963),
            CarModel("718 Boxster", 1996),
            CarModel("718 Cayman", 2005),
            CarModel("Macan", 2014),
            CarModel("Cayenne", 2002),
            CarModel("Panamera", 2009),
            CarModel("Taycan", 2019)
        ),
        "Ram" to listOf(
            CarModel("1500", 2010),
            CarModel("2500", 2010),
            CarModel("3500", 2010),
            CarModel("ProMaster", 2013)
        ),
        "Renault" to listOf(
            CarModel("Twingo", 1992, 2024),
            CarModel("Clio", 1990),
            CarModel("Captur", 2013),
            CarModel("Megane", 1995),
            CarModel("Austral", 2022),
            CarModel("Zoe", 2012, 2024)
        ),
        "Rolls-Royce" to listOf(
            CarModel("Ghost", 2009),
            CarModel("Wraith", 2013, 2023),
            CarModel("Dawn", 2015, 2023),
            CarModel("Phantom", 2017),
            CarModel("Cullinan", 2018),
            CarModel("Spectre", 2023)
        ),
        "Saab" to listOf(
            CarModel("900", 1978, 1998),
            CarModel("9-3", 1998, 2012),
            CarModel("9-5", 1997, 2012),
            CarModel("9-7X", 2004, 2009)
        ),
        "Seat" to listOf(
            CarModel("Ibiza", 1984),
            CarModel("Leon", 1999),
            CarModel("Arona", 2017),
            CarModel("Ateca", 2016),
            CarModel("Tarraco", 2018)
        ),
        "Skoda" to listOf(
            CarModel("Fabia", 1999),
            CarModel("Octavia", 1996),
            CarModel("Karoq", 2017),
            CarModel("Kamiq", 2019),
            CarModel("Kodiaq", 2016),
            CarModel("Superb", 2001),
            CarModel("Enyaq", 2021)
        ),
        "Smart" to listOf(
            CarModel("Fortwo", 1998),
            CarModel("Forfour", 2014, 2022),
            CarModel("#1", 2023)
        ),
        "Subaru" to listOf(
            CarModel("Impreza", 1992),
            CarModel("Crosstrek", 2012),
            CarModel("Legacy", 1989),
            CarModel("Outback", 1994),
            CarModel("Forester", 1997),
            CarModel("WRX", 2014)
        ),
        "Suzuki" to listOf(
            CarModel("Swift", 1983),
            CarModel("Ignis", 2016),
            CarModel("Baleno", 2015),
            CarModel("Vitara", 1988),
            CarModel("S-Cross", 2013),
            CarModel("Jimny", 2018)
        ),
        "Tesla" to listOf(
            CarModel("Model S", 2012),
            CarModel("Model X", 2015),
            CarModel("Model 3", 2017),
            CarModel("Model Y", 2020),
            CarModel("Cybertruck", 2023)
        ),
        "Toyota" to listOf(
            CarModel("Yaris", 1999),
            CarModel("Corolla", 1966),
            CarModel("Camry", 1982),
            CarModel("C-HR", 2016),
            CarModel("RAV4", 1994),
            CarModel("Land Cruiser", 1951),
            CarModel("Hilux", 1968),
            CarModel("Prius", 1997)
        ),
        "Volkswagen" to listOf(
            CarModel("Polo", 1975),
            CarModel("Golf", 1974),
            CarModel("T-Roc", 2017),
            CarModel("Tiguan", 2007),
            CarModel("Passat", 1973),
            CarModel("Jetta", 1979),
            CarModel("ID.4", 2020)
        ),
        "Volvo" to listOf(
            CarModel("S60", 2000),
            CarModel("V60", 2010),
            CarModel("XC40", 2017),
            CarModel("XC60", 2008),
            CarModel("XC90", 2002),
            CarModel("EX30", 2023),
            CarModel("EX90", 2023)
        ),
        "XPeng" to listOf(
            CarModel("G3", 2018),
            CarModel("P5", 2021),
            CarModel("P7", 2020),
            CarModel("G6", 2023),
            CarModel("G9", 2022)
        )
    )

    val manufacturers: List<String> = modelsByManufacturer.keys.toList()

    val years: List<String> = (currentYear + 1 downTo 1950).map { it.toString() }

    fun modelsFor(manufacturer: String): List<String> {
        return modelsByManufacturer[manufacturer]?.map { it.name } ?: emptyList()
    }

    fun yearsFor(manufacturer: String, model: String): List<String> {
        val carModel = modelsByManufacturer[manufacturer]
            ?.firstOrNull { it.name.equals(model, ignoreCase = true) }
            ?: return years
        val latestYear = carModel.endYear ?: (currentYear + 1)
        return (latestYear downTo carModel.startYear).map { it.toString() }
    }
}