grammar Enquanto;

programa : seqComando;     // sequência de comandos

seqComando: comando (';' comando)* ;

comando: ID ':=' expressao                          # atribuicao
       | 'skip'                                     # skip
       | 'se' bool 'entao' comando 'senao' comando  # se
       | 'enquanto' bool 'faca' comando             # enquanto
       | 'exiba' Texto                              # exiba
       | 'escreva' expressao                        # escreva
       | '{' seqComando '}'                         # bloco
       ;

expressao: ( exprAtom | exprAdd )                   # exprArit
         | 'leia'                                   # leia
         | '(' expressao ')'                        # expPar
         ;

exprAtom: INT                                       # inteiro
        | ID                                        # id
        ;

exprAdd: exprMul (( '+' | '-' ) exprMul)*
       ;

exprMul: exprPot (( '*' | '/' | '%' ) exprPot)*
       ;

exprPot: exprAtom ('^' exprAtom)*
       ;

bool: ('verdadeiro'|'falso')                        # booleano
    | expressao '=' expressao                       # opRel
    | expressao '<=' expressao                      # opRel
    | 'nao' bool                                    # naoLogico
    | bool 'e' bool                                 # eLogico
    | '(' bool ')'                                  # boolPar
    ;

INT: ('0'..'9')+ ;
ID: ('a'..'z')+;
Texto: '"' .*? '"';

Espaco: [ \t\n\r] -> skip;
