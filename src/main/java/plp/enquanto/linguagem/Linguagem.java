package plp.enquanto.linguagem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public interface Linguagem {
    final Map<String, Integer> ambiente = new HashMap<String, Integer>();
    final Scanner scanner = new Scanner(System.in);

    interface ExpressaoBase<T> {
        T getValor();
    }

    interface Bool extends ExpressaoBase<Boolean> {
    }

    interface Comando {
        public void execute();
    }

    interface Expressao extends ExpressaoBase<Integer> {
    }

    abstract class ExpBin implements Expressao {
        protected Expressao esq;
        protected Expressao dir;

        public ExpBin(Expressao esq, Expressao dir) {
            this.esq = esq;
            this.dir = dir;
        }
    }

    class Programa {
        private List<Comando> comandos;
        public Programa(List<Comando> comandos) {
            this.comandos = comandos;
        }
        public void execute() {
            for (Comando comando : comandos) {
                comando.execute();
            }
        }
    }

    class Se implements Comando {
        private Bool condicao;
        private Comando entao;
        private Comando senao;

        public Se(Bool condicao, Comando entao, Comando senao) {
            this.condicao = condicao;
            this.entao = entao;
            this.senao = senao;
        }

        @Override
        public void execute() {
            if (condicao.getValor())
                entao.execute();
            else
                senao.execute();
        }
    }

    Skip skip = new Skip();
    class Skip implements Comando {
        @Override
        public void execute() {
        }
    }

    class Escreva implements Comando {
        private Expressao exp;

        public Escreva(Expressao exp) {
            this.exp = exp;
        }

        @Override
        public void execute() {
            System.out.println(exp.getValor());
        }
    }

    class Enquanto implements Comando {
        private Bool condicao;
        private Comando faca;

        public Enquanto(Bool condicao, Comando faca) {
            this.condicao = condicao;
            this.faca = faca;
        }

        @Override
        public void execute() {
            while (condicao.getValor()) {
                faca.execute();
            }
        }
    }

    class Exiba implements Comando {
        public Exiba(String texto) {
            this.texto = texto;
        }

        private String texto;

        @Override
        public void execute() {
            System.out.println(texto);
        }
    }

    class Bloco implements Comando {
        private List<Comando> comandos;

        public Bloco(List<Comando> comandos) {
            this.comandos = comandos;
        }

        @Override
        public void execute() {
            for (Comando comando : comandos) {
                comando.execute();
            }
        }
    }

    class Atribuicao implements Comando {
        private String id;
        private Expressao exp;

        public Atribuicao(String id, Expressao exp) {
            this.id = id;
            this.exp = exp;
        }

        @Override
        public void execute() {
            ambiente.put(id, exp.getValor());
        }
    }

    class Inteiro implements Expressao {
        private int valor;

        public Inteiro(int valor) {
            this.valor = valor;
        }

        @Override
        public Integer getValor() {
            return valor;
        }
    }

    class Id implements Expressao {
        private String id;

        public Id(String id) {
            this.id = id;
        }

        @Override
        public Integer getValor() {
            final Integer v = ambiente.get(id);
            final int valor;
            if (v != null)
                valor = v;
            else
                valor = 0;

            return valor;
        }
    }

    Leia leia = new Leia();
    class Leia implements Expressao {
        @Override
        public Integer getValor() {
            return scanner.nextInt();
        }
    }

    class ExpSoma extends ExpBin {
        public ExpSoma(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Integer getValor() {
            return esq.getValor() + dir.getValor();
        }
    }

    class ExpSub extends ExpBin {
        public ExpSub(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Integer getValor() {
            return esq.getValor() - dir.getValor();
        }
    }

    class ExpMul extends ExpBin {
        public ExpMul(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Integer getValor() {
            return esq.getValor() * dir.getValor();
        }
    }

    class ExpDiv extends ExpBin {
        public ExpDiv(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Integer getValor() {
            return esq.getValor() / dir.getValor();
        }
    }

    class ExpPot extends ExpBin {
        public ExpPot(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Integer getValor() {
            return (int) Math.pow(esq.getValor(), dir.getValor());
        }
    }

    class Booleano implements Bool {
        private boolean valor;

        public Booleano(boolean valor) {
            this.valor = valor;
        }

        @Override
        public Boolean getValor() {
            return valor;
        }
    }

    abstract class ExpRel implements Bool {
        protected Expressao esq;
        protected Expressao dir;

        public ExpRel(Expressao esq, Expressao dir) {
            this.esq = esq;
            this.dir = dir;
        }
    }

    public class ExpDesigual extends ExpIgual {

        public ExpDesigual(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Boolean getValor() {
            return !super.getValor();
        }

    }

    public class ExpIgual extends ExpRel {

        public ExpIgual(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Boolean getValor() {
            return esq.getValor() == dir.getValor();
        }

    }

    public class ExpMaior extends ExpRel {
        public ExpMaior(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Boolean getValor() {
            return esq.getValor() > dir.getValor();
        }
    }

    public class ExpMaiorIgual extends ExpRel {
        public ExpMaiorIgual(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Boolean getValor() {
            return esq.getValor() >= dir.getValor();
        }
    }

    public class ExpMenor extends ExpRel {
        public ExpMenor(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Boolean getValor() {
            return esq.getValor() < dir.getValor();
        }
    }

    public class ExpMenorIgual extends ExpRel {
        public ExpMenorIgual(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Boolean getValor() {
            return esq.getValor() <= dir.getValor();
        }
    }

    public class NaoLogico implements Bool {
        private Bool b;

        public NaoLogico(Bool b) {
            this.b = b;
        }

        @Override
        public Boolean getValor() {
            return !b.getValor();
        }
    }

    public class ELogico implements Bool {
        private Bool esq;
        private Bool dir;

        public ELogico(Bool esq, Bool dir) {
            this.esq = esq;
            this.dir = dir;
        }

        @Override
        public Boolean getValor() {
            return esq.getValor() && dir.getValor();
        }
    }

    public class OuLogico implements Bool {
        private Bool esq;
        private Bool dir;

        public OuLogico(Bool esq, Bool dir) {
            this.esq = esq;
            this.dir = dir;
        }

        @Override
        public Boolean getValor() {
            return esq.getValor() || dir.getValor();
        }
    }

    public class XorLogico implements Bool {
        private Bool esq;
        private Bool dir;

        public XorLogico(Bool esq, Bool dir) {
            this.esq = esq;
            this.dir = dir;
        }

        @Override
        public Boolean getValor() {
            return esq.getValor() ^ dir.getValor();
        }
    }
}
