public class Main {
    public static void main(String[] args) {
        //vizitka
        System.out.println("vizitka");
        System.out.println("menya zovutIrina");
        System.out.println("mne 18 let");
        System.out.println("ya jivu v karagande");
        System.out.println();

        //peremennie
        int cifra = 1;
        double pi = 3.14;
        boolean sdamna5 = true;
        String aplusgrade = "spasibo!";
        char bukva = 'A';
        System.out.println("random cifra = (int): " + cifra);
        System.out.println("chislo pi ravno = (double): " + pi);
        System.out.println("pravda li chto chernikova irina sdast javu na 5? (boolean): " + sdamna5);
        System.out.println("ocenka budet krytaya > (char): "+ bukva + ", (string): "+ aplusgrade);
        System.out.println();

        //umnoj na 7
        for (int i = 1; i<=10; i++) {
            System.out.println("7 * " + i + "= " + (7*i));
        }
        System.out.println();
        
        //uzor
        int rows = 5;
        for (int i = 1; i <= rows; i++) {
            for (int s = 1; s <= rows - i; s++) {
            System.out.print("   ");
        }
        for (int j = 1; j <= 2 * i - 1; j++) {
            System.out.print(" * ");
        }

        }
    }
}
        