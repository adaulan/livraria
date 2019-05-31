/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SERVLETS;

import DAO.CarrinhoDAO;
import DAO.ClienteDAO;
import DAO.FormasDePagamentoDAO;
import DAO.LivroDAO;
import DAO.VendaDAO;
import Modal.Cliente;
import Modal.FormaDePagamento;
import Modal.ItensCarrinho;
import Modal.Livro;
import Modal.Venda;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author mt12732
 */
@WebServlet(name = "CadastroVenda", urlPatterns = {"/CadastroVenda"})
public class CadastroVenda extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession();
        //Lista de Clientes no Select
        try {
            List<Cliente> listaClientes = ClienteDAO.listar();
            request.setAttribute("listaClientes", listaClientes);
            sessao.setAttribute("listaClientes", listaClientes);
        } catch (Exception e) {
            System.out.println(e);
        }
        //Lista de Produtos no Select
        try {
            List<Livro> listaProduto = LivroDAO.listar();
            request.setAttribute("listaProduto", listaProduto);
            sessao.setAttribute("listaProduto", listaProduto);
        } catch (Exception e) {
            System.out.println(e);
        }
        //Lista de Formas de Pagamento no Select
        try {
            List<FormaDePagamento> listaPagamento = FormasDePagamentoDAO.listarFormaPagamento();
            request.setAttribute("listaPagamento", listaPagamento);
            sessao.setAttribute("listaPagamento", listaPagamento);
        } catch (Exception e) {
            System.out.println(e);
        }
        //Funcionalidade de Venda
        try {
            if (request.getParameter("produto") != null) {
                //ID do livro selecionado
                int IDLivro = Integer.parseInt(request.getParameter("produto"));
                //Pega as informações do Livro no BD
                Livro L = LivroDAO.procurarId(IDLivro);
                //Testa se o ID da Venda já foi criado
                

                if (sessao.getAttribute("IDVenda") == null) {
                    
                    Cliente cliente = ClienteDAO.procurarId(Integer.parseInt(request.getParameter("cliente")));
                    sessao.setAttribute("NomeCliente", cliente.getNome());
                    request.setAttribute("NomeCliente", cliente.getNome());
                    
                    System.out.println(request.getAttribute("NomeCliente"));
                    
                    
                    int IDVenda = VendaDAO.criarIDVenda();

                    sessao.setAttribute("IDVenda", IDVenda);
                    request.setAttribute("IDVenda", IDVenda);
                    LocalDate data = LocalDate.now();
                    Venda v = new Venda();

                    v.setIDCliente(Integer.parseInt(request.getParameter("cliente")));
                    request.setAttribute("IDCliente", v.getIDCliente());
                    
                    v.setIDVenda(IDVenda);
                    v.setDataVenda(data.toString());
                    v.setValor(L.getValorVenda());
                    v.setFormaPagamento("Dinheiro");

                    VendaDAO.inserirVenda(v);
                } else {
                    
                    
                    LocalDate data = LocalDate.now();
                    Venda v = new Venda();

                    v.setIDCliente(Integer.parseInt(request.getParameter("cliente")));
                    System.out.println("ID do Cliente segunda venda: " + v.getIDCliente());
                    
                    request.setAttribute("IDCliente", v.getIDCliente());
                    System.out.println("ID do cliente no request: " + request.getAttribute("IDCliente") );
                    
                    Cliente cliente = ClienteDAO.procurarId(v.getIDCliente());
                    sessao.setAttribute("NomeCliente", cliente.getNome());
                    request.setAttribute("NomeCliente", cliente.getNome());
                    
                    
                    v.setIDVenda((int) sessao.getAttribute("IDVenda"));
                    v.setDataVenda(data.toString());
                    v.setValor(Float.parseFloat(request.getParameter("valorTotal")) + L.getValorVenda());
                    v.setFormaPagamento("Dinheiro");
                    System.out.println("ANTES DO UPDATE");
                    
                    VendaDAO.update(v);
                }
                //Cria um novo Carrinho
                ItensCarrinho C = new ItensCarrinho();

                C.setIDLivro(IDLivro);
                C.setIDcarrinho((int) sessao.getAttribute("IDVenda"));
                System.out.println("IDCARRINHO = " + (int) sessao.getAttribute("IDVenda"));
                C.setQuantidade(1);
                C.setValor(L.getValorVenda());

                CarrinhoDAO.inserir(C);

                List<Livro> listaProduto = CarrinhoDAO.listar(C.getIDcarrinho());
                request.setAttribute("listaProdutoCarrinho", listaProduto);
                sessao.setAttribute("listaProdutoCarrinho", listaProduto);

                request.setAttribute("produto", L);
                sessao.setAttribute("produto", L);

            } else {
                Livro L = new Livro();
                request.setAttribute("produto", L);
                sessao.setAttribute("produto", L);

                List<Livro> listaProduto = null;
                listaProduto.add(L);
                request.setAttribute("listaProdutoCarrinho", listaProduto);
                sessao.setAttribute("listaProdutoCarrinho", listaProduto);
                request.setAttribute("IDVenda", null);
                sessao.setAttribute("IDVenda", null);

            }

        } catch (Exception e) {
            System.out.println(e);
        }

        RequestDispatcher dispatcher
                = request.getRequestDispatcher("/JSP-PAGES/CadastroVenda.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int idLivro = Integer.parseInt(request.getParameter("ID"));
        String Titulo = request.getParameter("NomeLivro");
        String Autor = request.getParameter("Autor");
        String Editora = request.getParameter("Editora");
        int Valor = Integer.parseInt(request.getParameter("ValorVenda"));
        int Quantidade = Integer.parseInt(request.getParameter("Quantidade"));
        //adicionar livro no carrinho
        Livro l = new Livro(Titulo, Autor, Editora, Valor, idLivro, Quantidade);

        //montar o tabela com o carrinho
        RequestDispatcher dispatcher
                = request.getRequestDispatcher("/JSP-PAGES/Home.jsp");
        dispatcher.forward(request, response);
    }

}
