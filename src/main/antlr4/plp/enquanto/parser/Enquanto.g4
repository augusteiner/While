grammar Enquanto;

programa: seqInstr
        ;

decl: declFuncao
    ;

instr: decl                                         # instrucao
     | comando                                      # instrucao
     ;

seqInstr: instr (';' instr)*
        ;

seqComando: comando (';' comando)*
          ;

argList: (ID (',' ID)*)?
       ;

paramList: (expressao (',' expressao)*)?
         ;

execFuncao: ID '(' paramList ')'
         ;

declFuncao: ID '(' argList ')' '=' expressao
         ;

comando: ID ':=' expressao                          # atribuicao
       | 'skip'                                     # skip
       | 'se' bool 'entao' comando
         ( 'senaose' bool 'entao' comando )*
         'senao' comando                            # se
       | 'para' ID
         'de' expressao
         'ate' expressao ('passo' INT)?
         'faca' comando                             # para
       | 'enquanto' bool 'faca' comando             # enquanto
       | 'escolha' expressao
         ( 'caso' INT ':' comando )*
         'outro' ':' comando                        # escolha
       | 'exiba' Texto                              # exiba
       | 'escreva' expressao                        # escreva
       | execFuncao                                 # funcao
       | '{' seqComando '}'                         # bloco
       ;

expressao: ( exprAtom | exprAdd )                   # exprArit
         | 'leia'                                   # leia
         | ( '-' ) expressao                        # exprNeg
         | execFuncao                               # exprExecFuncao
         ;

exprAtom: INT                                       # inteiro
        | ID                                        # id
        | '(' expressao ')'                         # exprPar
        ;

exprAdd: exprMul (( '+' | '-' ) exprMul)*
       | '(' exprMul ')'
       ;

exprMul: exprPot (( '*' | '/' | '%' ) exprPot)*
       | '(' exprPot ')'
       ;

exprPot: exprAtom ('^' exprAtom)*
       | '(' exprAtom ')'
       ;

bool: ('verdadeiro'|'falso')                        # booleano
    | 'nao' bool                                    # naoLogico
    | expressao (operador_rel expressao)+           # exprBool
    | bool (operador_bool bool)+                    # exprBool
    | '(' bool ')'                                  # boolPar
    ;

operador_rel: '=' | '<=' | '>=' | '<>' | '<' | '>' ;
operador_bool: 'e' | 'ou' | 'xor' ;

INT: ('0'..'9')+ ;
ID: ('a'..'z')+ ;
Texto: '"' .*? '"' ;

Espaco: [ \t\n\r] -> skip;
