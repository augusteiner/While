package plp.enquanto.linguagem;

import java.util.*;

public interface Linguagem {
    Ambiente AMBIENTE = new Ambiente();
    Scanner SCANNER = new Scanner(System.in);

    interface ExpressaoBase<T> {
        T getValor();
    }

    interface Bool extends ExpressaoBase<Boolean> {
    }

    interface Instrucao {
    }

    interface Comando extends Instrucao {
        void execute();
    }

    interface Expressao extends ExpressaoBase<Integer> {
    }

    abstract class ExpBin implements Expressao {
        Expressao esq;
        Expressao dir;

        ExpBin(Expressao esq, Expressao dir) {
            this.esq = esq;
            this.dir = dir;
        }
    }

    class Ambiente {
        final Map<String, Integer> ids;
        final Ambiente legado;

        Ambiente() {
            this.ids = new HashMap<String, Integer>();
            this.legado = this;
        }

        public Ambiente(Ambiente legado) {
            this.ids = new HashMap<String, Integer>();
            this.legado = legado;
        }

        public Ambiente getLegado() {
            return legado;
        }

        Integer get(String id) {
            if (!ids.containsKey(id)) {
                return legado.get(id);
            } else {
                return ids.get(id);
            }
        }

        void put(String id, Integer valor) {
            ids.put(id, valor);
        }
    }

    class Programa {
        private List<Instrucao> comandos;

        public Programa(List<Instrucao> comandos) {
            this.comandos = comandos;
        }

        public void execute() {
            for (Instrucao comando : comandos) {
                if (comando instanceof Comando)
                    ((Comando) comando).execute();
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

    class ChamadaFuncao implements Expressao {
        private Funcao funcao;
        private List<Expressao> params;

        public ChamadaFuncao(Funcao funcao, List<Expressao> params) {
            this.params = params;
            this.funcao = funcao;
        }

        @Override
        public Integer getValor() {
            for (int i = 0; i < funcao.args.size(); i++) {
                String id = funcao.args.get(i);

                funcao.ambiente.put(id, params.get(i).getValor());
            }

            return funcao.retorno.getValor();
        }
    }

    class Funcao implements Instrucao {
        private String id;
        private Expressao retorno;

        private Ambiente ambiente;
        private List<String> args;

        public Funcao(String id, Ambiente ambiente) {
            this.id = id;
            this.ambiente = ambiente;
        }

        public void setArgs(List<String> args) {
            this.args = args;
        }

        public void setRetorno(Expressao retorno) {
            this.retorno = retorno;
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            String argDelim = "";

            for (String arg : args) {
                buffer.append(argDelim);
                buffer.append(arg);

                argDelim = ", ";
            }

            return String.format("%s(%s)", id, buffer.toString());
        }
    }

    class Atribuicao implements Comando {
        private Ambiente ambiente;

        private String id;
        private Expressao exp;

        public Atribuicao(String id, Expressao exp, Ambiente ambiente) {
            this.ambiente = ambiente;

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
        private Ambiente ambiente;
        private String id;

        public Id(String id, Ambiente ambiente) {
            this.ambiente = ambiente;
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

        void setValor(Integer valor) {
            ambiente.put(id, valor);
        }
    }

    Leia leia = new Leia();
    class Leia implements Expressao {
        @Override
        public Integer getValor() {
            return SCANNER.nextInt();
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
        Expressao esq;
        Expressao dir;

        ExpRel(Expressao esq, Expressao dir) {
            this.esq = esq;
            this.dir = dir;
        }
    }

    class ExpDesigual extends ExpIgual {
        public ExpDesigual(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Boolean getValor() {
            return !super.getValor();
        }
    }

    class ExpIgual extends ExpRel {
        public ExpIgual(Expressao esq, Expressao dir) {
            super(esq, dir);
        }

        @Override
        public Boolean getValor() {
            return esq.getValor().equals(dir.getValor());
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

    class NaoLogico implements Bool {
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
