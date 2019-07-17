package my.garden.daoImpl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import my.garden.dao.BoardQnADAO;
import my.garden.dto.BoardQnADTO;
import my.garden.dto.BoardReviewDTO;


@Repository
public class BoardQnADAOImpl implements BoardQnADAO{
	@Autowired
	private SqlSessionTemplate sst;
	
	public int writeQnA(BoardQnADTO dto) {	//글작성	
		return sst.insert("boardQnAMB.writeQnA",dto);
	}
	
	public List<BoardQnADTO> qnaList(int bq_p_no, int startNum2, int endNum2){ //상세페이지에서 Q&A 목록
		System.out.println("dao왔음");
		Map<String,Integer> map = new HashMap<>();
		map.put("bq_p_no", bq_p_no);
		map.put("startNum2",startNum2);
		map.put("endNum2", endNum2);
		return sst.selectList("boardQnAMB.qnaList",map);
	}

	public int qnaCount(int bq_p_no) { //상세페이지에서 총  Q&A 개수(상품별  Q&A 개수)
		return sst.selectOne("boardQnAMB.qnaCount", bq_p_no);
	}


	/*페이지 네비게이터*/
	public String getNavi(int qnaPage,int bq_p_no){

		int recordTotalCount = qnaCount(bq_p_no);
		int recordCountPerPage = 5; //5개의 글이 보이게 한다.	
		int naviCountPerPage = 5; //5개의 네비가 보이게 한다.

		int pageTotalCount = recordTotalCount / recordCountPerPage;
		if(recordTotalCount % recordCountPerPage > 0) {
			pageTotalCount++;
		}

		//현재  페이지 오류 검출 및 정정
		/*보안코드 : 현재페이지가 1보다 작다면 1로, 전체페이지보다 크다면 전체페이지(pageTotalCount)로 표시하겠다*/
		if(qnaPage < 1) {
			qnaPage = 1;
		}else if(qnaPage > pageTotalCount) {
			qnaPage = pageTotalCount;
		}
		int startNavi2 = (qnaPage - 1)/naviCountPerPage * naviCountPerPage + 1;
		int endNavi2 = startNavi2 + (naviCountPerPage - 1); 

		if(endNavi2 > pageTotalCount) {
			endNavi2 = pageTotalCount;
		}

		System.out.println("현재 위치 : " + qnaPage);
		System.out.println("네비 시작 : " + startNavi2);
		System.out.println("네비 끝 : " + endNavi2);

		boolean needPrev = true;
		boolean needNext = true;

		if(startNavi2 == 1) { 
			needPrev = false;
		}
		if(endNavi2 == pageTotalCount) {
			needNext = false;
		}

		StringBuilder sb = new StringBuilder();

		if(needPrev) {
			int prevStartNavi2 = startNavi2-1;
			sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"productsRead?pnumber="+bq_p_no+"&revPage=1&qnaPage="+prevStartNavi2+"\" aria-label=\"Previous\"><span aria-hidden=\"true\">&laquo;</span></a></li>");

		}
		for(int i = startNavi2; i <= endNavi2; i++) {
			sb.append("<li class=\"page-item\"><a class=\"page-link qnaPageNumber pageNumber\" href=\"productsRead?pnumber="+bq_p_no+"&revPage=1&qnaPage="+ i +"\">" + i + "</a></li>");
		}
		if(needNext) {
			int nextEndNavi2 = endNavi2+1;
			sb.append("<li class=\"page-item\"><a class=\"page-link\" href=\"productsRead?pnumber="+bq_p_no+"&revPage=1&qnaPage="+ nextEndNavi2++ +"\""+ 
					"							aria-label=\"Next\"> <span aria-hidden=\"true\">&raquo;</span>" + 
					"						</a></li>");
		}

		return sb.toString();
	}

	public BoardQnADTO readQnA(int bq_no, String mine) {		
		return sst.selectOne("boardQnAMB.readQnA", bq_no);
	}
	
	public int updateQnA(BoardQnADTO dto, int bq_no) {
//		Map<String,Object> map = new HashMap<>();
//		map.put("bq_checkedSecret", dto.getBq_checkedSecret());
//		map.put("bq_title", dto.getBq_title());
//		map.put("bq_content", dto.getBq_content());
//		map.put("bq_imagepath1", dto.getBq_imagepath1());
//		map.put("bq_imagepath2", dto.getBq_imagepath2());
//		map.put("bq_imagepath3", dto.getBq_imagepath3());
//		
		return sst.update("boardQnAMB.updateQnA", dto);
	}
	
	/*관리자확인*/
	public String checkAdmin(String m_email) {
		return sst.selectOne("boardQnAMB.checkAdmin", m_email);
	}
	
	public int writeComment() {
		return sst.insert("boardQnAMB.writeComment");
	}
	public int setAnsY(int cq_no) {
		return sst.update("boardQnAMB.setAnsY");	}
	
}