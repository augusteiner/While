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

    class Para implements Comando {
        final Id id;

        final Expressao de;
        final Expressao ate;
        final Expressao passo;

        final Comando faca;

        public Para(Id id, Expressao de, Expressao ate, Expressao passo, Comando faca) {
            this.id = id;

            this.de = de;
            this.ate = ate;
            this.passo = null == passo ? new Inteiro(1) : passo;

            this.faca = faca;
        }

        @Override
        public void execute() {
            id.setValor(de.getValor());

            for (
                int i = id.getValor();
                i <= ate.getValor();
                i += passo.getValor(), id.setValor(i)
            ) {
                faca.execute();
            }
        }
    }

    class SenaoSe implements Comando {
        private Bool condicao;
        private Comando entao;

        public SenaoSe(Bool condicao, Comando entao) {
            this.condicao = condicao;
            this.entao = entao;
        }

        @Override
        public void execute() {
            entao.execute();
        }
    }

    class Se extends SenaoSe implements Comando {
        private Bool condicao;
        private Comando senao;
        private List<SenaoSe> listaSenaoSe;

        public Se(Bool condicao, Comando entao, List<SenaoSe> listaSenaoSe, Comando senao) {
            super(condicao, entao);

            this.listaSenaoSe = listaSenaoSe;
            this.condicao = condicao;
            this.senao = senao;
        }

        @Override
        public void execute() {
            boolean executaSenao = true;

            if (condicao.getValor()) {
                executaSenao = false;

                super.execute();
            } else {
                for (SenaoSe senaoSe : listaSenaoSe) {
                    if (senaoSe.condicao.getValor()) {
                        executaSenao = false;

                        senaoSe.execute();
                        break;
                    }
                }
            }

            if (executaSenao) {
                senao.execute();
            }
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

    class Escolha implements Comando {
        private Expressao padrao;
        private Map<Expressao, Comando> comandos;
        private Comando outro;

        public Escolha(Expressao padrao, Map<Expressao, Comando> comandos, Comando outro) {
            this.padrao = padrao;
            this.comandos = comandos;
            this.outro = outro;
        }

        @Override
        public void execute() {
            boolean executaOutro = true;
            int valor = padrao.getValor();

            for (Expressao exp : comandos.keySet()) {
                if (exp.getValor() == valor) {
                    executaOutro = false;

                    comandos.get(exp).execute();
                    // XXX: pára a execução por padrão
                    break;
                }
            }

            if (executaOutro) {
                outro.execute();
            }
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

        public void setValor(Integer valor) {
            ambiente.put(id, valor);
        }
    }

    Leia leia = new Leia();
    class Leia implements Expressao {
        @Override
        public Integer getValor() {
            return scanner.nextInt();
        }
    }

    class ExpNeg implements Expressao {
        private Expressao exp;

        public ExpNeg(Expressao exp) {
            this.exp = exp;
        }

        @Override
        public Integer getValor() {
            return -exp.getValor();
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
            int divisor = dir.getValor();

            if (divisor == 0) {
                throw new ArithmeticException("Divisão por zero");
            }

            return esq.getValor() / divisor;
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
