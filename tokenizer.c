/*
 * tokenizer.c delar upp en text i sina ord och matar ut orden
 * omvandlade till små bokstäver, ett ord per rad följt av ordets
 * teckenposition i texten. Ett ord definieras som en eller flera
 * på varandra följande bokstäver i det svenska alfabetet.
 * Copyright Viggo Kann, viggo@nada.kth.se, 1999
 *
 * Lätt omhackad av Jakob Nordström 2003 för att klara tecken med accenter
 * (t.ex. á, è. ü osv.).
 *
 * Fixad av Felix Almay 2022 för att hantera ß-tecken samt förbättra läsbarheten.
 */

#include <stdio.h>
#include <stdlib.h>

 /* alfabet i Latin-1-ordning */
#define ALPHABET "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÅÖ"

static unsigned char   u2l[256];
static long            pos = 0L;

static int readChar() {
    pos++;
    return getchar();
}

static void tokenize() {
   unsigned char   buf[1000];
   unsigned char  *s;
   int             ch;
   long            startpos;

   do {
      for (ch = readChar(); ch != EOF && u2l[ch] == 0; ch = readChar()) ;
      if (ch == EOF) break;
      startpos = pos-1;
      s = buf;
      *s++ = u2l[ch];
      for (ch = readChar(); ch != EOF && u2l[ch]; ch = readChar())
         *s++ = u2l[ch];
      *s = '\0';
      printf("%s %ld\n", buf, startpos);
   } while (ch != EOF);
}

static void initialize() {
   unsigned char ch;

   for (int i = 0; i < 256; i++)
      u2l[i] = 0;

   for (unsigned char* s = (unsigned char *) ALPHABET; *s; s++) {
      ch = *s + 'a' - 'A';
      u2l[*s] = u2l[ch] = ch;
   }

   /*
    * Nedan följer speciallösning för att klara accenterade tecken
    */
      
   ch = 223;         /* tyskt dubbel-s */
   u2l[ch] = 's';

   for (ch = 224; ch <= 227; ++ch) /* a med accent (utom å och ä) */
      u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'a';
    
   /* 228 är ä och 229 är ö - ingen förändring */
   
   ch = 230;			/* ae till ä */
   u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'ä';

   ch = 231;			/* c med cedilj till c*/
   u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'c';

   for (ch = 232; ch <= 235; ++ch) /* e med accent (även é) */
      u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'e';

   for (ch = 236; ch <= 239; ++ch) /* i med accent */
      u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'i';
    
   ch = 240;			/* eth till d */
   u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'd';
    
   ch = 241;			/* n med ~ till n */
   u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'n';

   for (ch = 242; ch <= 245; ++ch) /* o med accent (förutom ö) */
      u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'o';

   /* 246 är ö - ingen förändring */
   /* 247 är divisionstecken - ingen rimlig bokstav */
   
   ch = 248;			/* o genomskuret till ö */
   u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'ö'; 

   for (ch = 249; ch <= 252; ++ch) /* u med accent */
   u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'u';

   ch = 253;  /* y med accent */
   u2l[ch +  - 'a' + 'A'] = u2l[ch] = 'y';
   ch = 255;
   u2l[ch] = 'y';
   
   /*
   * Kontrollutskrift
   *
   */
   
   /*
   for (ch = 65; ch < 255; ++ch) {
      printf ("Kod %u tecken %c lagrat %c \n",
      (unsigned) ch, ch, u2l[ch]);
   }

   ch = 255;
   printf ("Kod %u tecken %c lagrat %c \n",
      (unsigned) ch, ch, u2l[ch]);
   */
}

int main() {
    initialize();
    tokenize();
}
