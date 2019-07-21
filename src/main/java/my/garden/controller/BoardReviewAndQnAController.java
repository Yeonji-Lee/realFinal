package my.garden.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import my.garden.dto.BoardQnADTO;
import my.garden.dto.BoardReviewDTO;
import my.garden.dto.CommentFreeDTO;
import my.garden.dto.CommentQnADTO;
import my.garden.dto.MembersDTO;
import my.garden.dto.ProductsDTO;
import my.garden.service.BoardQnAService;
import my.garden.service.BoardReviewService;
import my.garden.service.ProductsService;
import my.garden.serviceImpl.LoginService;



@Controller
public class BoardReviewAndQnAController {

	@Autowired
	private HttpSession session;
	@Autowired
	private BoardReviewService brService;
	@Autowired
	private BoardQnAService qnaService;
	@Autowired
	LoginService loginservice;

	@Autowired
	private ProductsService pservice;


	@RequestMapping("productsRead")
	public String toProductsRead(int pnumber, Model model, HttpServletRequest request, int revPage, int qnaPage) {

		revPage = Integer.parseInt(request.getParameter("revPage"));
		qnaPage = Integer.parseInt(request.getParameter("qnaPage"));

		int br_p_no = pnumber; 

		int startNum = (5 * revPage) - 4;
		int endNum = startNum + 4;

		int startNum2 = (5 * qnaPage) - 4;
		int endNum2 = startNum2 + 4;

		List<BoardReviewDTO> reviewList = null;
		List<BoardQnADTO> qnaList = null;

		try {
			request.setAttribute("reviewList", brService.reviewList(br_p_no, startNum, endNum));
			request.setAttribute("qnaList", qnaService.qnaList(br_p_no, startNum2, endNum2));
			request.setAttribute("getNavi", brService.getNavi(revPage, br_p_no));
			request.setAttribute("getNaviForQnA", qnaService.getNavi(qnaPage, br_p_no));
			request.setAttribute("revPage", revPage);
			request.setAttribute("qnaPage", qnaPage);
			session.setAttribute("pnumber", pnumber); 
		} catch (Exception e) {
			e.printStackTrace();
		}

		try{
			ProductsDTO dto = pservice.selectOneProductService(pnumber);
			model.addAttribute("result", dto);
			return "products/productsRead";
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping("reviewWriteForm")
	public String reviewWriteForm(Model model) {	
		int pnumber = (int) session.getAttribute("pnumber");
		ProductsDTO dto = pservice.selectOneProductService(pnumber);
		model.addAttribute("productInfo", dto);
		return "/boardProducts/reviewWriteForm";
	}

	@RequestMapping("writeReview")
	public String writeReview(BoardReviewDTO dto, MultipartFile image) {

		String id = (String) session.getAttribute("loginId"); 
		int br_p_no = (int) session.getAttribute("pnumber");
		String name = (String) session.getAttribute("loginName");
		System.out.println("name : " + name);
		dto.setBr_p_no(br_p_no);
		dto.setBr_email(id);
		dto.setBr_name(name);

		//System.out.println("이미지" + image);
		String path = "D:\\SpringOnly\\finalProject\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\MyGarden\\resources\\";
		File dir = new File(path + id + "/"); //폴더경로
		//System.out.println("폴더 존재? : " + dir.isDirectory());
		if(!dir.isDirectory()) { // 폴더가 있는지 확인.
			System.out.println("폴더생성");
			dir.mkdirs(); // 없으면 생성
		}
		String resourcePath = session.getServletContext().getRealPath("/resources/"+id);
		System.out.println("resourcePath : " + resourcePath);

		File newFile = new File(resourcePath + "/" + System.currentTimeMillis() + "_review.png");
		try {
			image.transferTo(newFile);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String filePath = "/resources/"+ id +"/" + newFile.getName();
		System.out.println("filePath : " + filePath);
		dto.setBr_imagepath(filePath);

		try {
			brService.writeReview(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("controller");
		//return "redirect:reviewAndQnA?br_p_no=1&currentPage=1&qnaCurrentPage=1"; //나중에 고치기..글번호
		return "redirect:productsRead?&revPage=1&qnaPage=1&pnumber=" + br_p_no;
	}


	@ResponseBody
	@RequestMapping("recommend")
	public Map<String, Object> recommend(HttpServletRequest request, BoardReviewDTO dto) {
		String br_email = (String) session.getAttribute("loginId");
		int br_no = Integer.parseInt(request.getParameter("br_no"));
		String br_title = request.getParameter("br_title");
		//		System.out.println("br_no (recommendCount): " + br_no);
		//		System.out.println("br_title (recommendCount): " + br_title);
		//		System.out.println("br_email (recommendCount): " + br_email);
		int br_recommend = dto.getBr_recommend();
		//		System.out.println("br_recommend : " + br_recommend);
		Map<String, Object> result = new HashMap<>();
		//	result.put("br_no", br_no);
		try {
			//			System.out.println("원래 도움돼요 수 : " + brService.recommendCount(br_recommend, br_no));
			int recommendCheck = brService.recommendCheck(br_no, br_email);
			if(recommendCheck > 0) { //도움돼요 취소
				brService.recommendDelete(br_no, br_email);
				result.put("recommendDelete", 0);
				System.out.println("도움돼요 취소");
			}else { //도움돼요 (추천하기)
				brService.recommendUpdate(br_email, br_no, br_title);
				result.put("recommendUpdate", 1);
				System.out.println("도움돼요");
			}
			int recommendCount = brService.recommendCount(br_recommend, br_no);
			result.put("recommendCount", recommendCount);
			//			System.out.println("도움돼요 수 : " + brService.recommendCount(br_recommend, br_no));
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping("reviewUpdateForm")
	public String reviewUpdateForm(HttpServletRequest request, int br_no) {
		int pnumber = (int) session.getAttribute("pnumber");
		ProductsDTO dto = pservice.selectOneProductService(pnumber);
		br_no = Integer.parseInt(request.getParameter("br_no"));
		try {
			request.setAttribute("oneReview", brService.oneReview(br_no));
			request.setAttribute("productInfo", dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/boardProducts/reviewUpdateForm";
	}

	@RequestMapping("reviewUpdate")
	public String reviewUpdate(HttpServletRequest request, BoardReviewDTO dto, MultipartFile image) {
		int br_no = Integer.parseInt(request.getParameter("br_no"));
		String br_title = request.getParameter("br_title");
		String br_content = request.getParameter("br_content");
		String id = (String) session.getAttribute("loginId"); 
		Map<String, Object> map = new HashMap<>();
		//		map.put("br_imagepath", dto.getBr_imagepath());
		System.out.println("br_imagepath 처음 : "+dto.getBr_imagepath());
		System.out.println("image" + image);
		if(image!=null) {

			String resourcePath = session.getServletContext().getRealPath("/resources/"+id);
			System.out.println("resourcePath : " + resourcePath);

			File newFile = new File(resourcePath + "/" + System.currentTimeMillis() + "_review.png");
			try {
				image.transferTo(newFile);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String filePath = "/resources/"+ id +"/" + newFile.getName();
			System.out.println("filePath : " + filePath);
			map.put("br_imagepath", filePath);
		}



		map.put("br_title", br_title);
		map.put("br_content", br_content);
		map.put("br_no", br_no);



		try {
			brService.updateReview(map);
			request.setAttribute("oneReview", brService.oneReview(br_no));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "/boardProducts/reviewUpdateForm";
	}

	@RequestMapping("reviewDelete")
	public String reviewDelete(HttpServletRequest request, int br_no) {
		br_no = Integer.parseInt(request.getParameter("br_no"));
		try {
			brService.deleteReview(br_no);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:productsRead?&revPage=1&qnaPage=1&pnumber=" + br_no;
	}

	@RequestMapping("qnaWriteForm")
	public String qnaWriteForm(int bq_p_no) {

		return "/boardProducts/qnaWriteForm";
	}
	@RequestMapping("writeQnA")
	public String writeQnA(HttpServletRequest request, BoardQnADTO dto, MultipartFile[] images) {
		String checkedSecret = request.getParameter("checkedSecret");
		if(checkedSecret==null) {
			dto.setBq_checkedSecret("n");
		}else{
			dto.setBq_checkedSecret("y");
		}
		String id = (String) session.getAttribute("loginId");
		String name = (String) session.getAttribute("loginName");
		//String writer = (String) session.getAttribute("loginName");
		//System.out.println("loginId : " + id);
		int bq_p_no = (int) session.getAttribute("pnumber");
		dto.setBq_email(id);
		dto.setBq_name(name);
		dto.setBq_p_no(bq_p_no);
		//System.out.println(dto.getBq_title());

		//	if(!images.isEmpty()) { //이미지 들어있으면 
		if(images.length>0) {

			String path = "D:\\SpringOnly\\finalProject\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\MyGarden\\resources\\";
			File dir = new File(path + id + "/"); //폴더경로
			if(!dir.isDirectory()) { // 폴더가 있는지 확인.
				System.out.println("폴더생성");
				dir.mkdirs(); // 없으면 생성
			}
			String resourcePath = session.getServletContext().getRealPath("/resources/"+id);
			System.out.println("resourcePath : " + resourcePath);


			String[] filePath = new String[images.length];
			for(int i = 0;i < images.length; i++) {
				File newFile = new File(resourcePath + "/" + System.currentTimeMillis() + i + "_QnA.png");
				try {

					images[i].transferTo(newFile);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				filePath[i] = "/resources/"+ id +"/" + newFile.getName();
			}
			dto.setBq_imagepath1(filePath[0]);
			dto.setBq_imagepath2(filePath[1]);
			dto.setBq_imagepath3(filePath[2]);
		}


		try {
			qnaService.writeQnA(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:productsRead?&mine=y&revPage=1&qnaPage=1&pnumber=" + bq_p_no;

	}

	@RequestMapping("readQnA")
	public String readQnA(HttpServletRequest request, int bq_no, String mine, String checkA) throws Exception {	
		String id = (String) session.getAttribute("loginId");
		MembersDTO mdto = new MembersDTO();
		request.setAttribute("writerInfo", loginservice.memSelectAll(mdto, id));

		request.setAttribute("mine", mine);
		request.setAttribute("readQnA", qnaService.readQnA(bq_no, mine));
		request.setAttribute("commentList", qnaService.commentList(bq_no));
		System.out.println(checkA);
		request.setAttribute("checkAns", checkA);
		return "/boardProducts/qnaRead";
	}

	@RequestMapping("updateQnAForm")
	public String updateQnAForm(HttpServletRequest request,int bq_no) throws Exception {	
		request.setAttribute("readQnA", qnaService.readQnA(bq_no, "y"));		
		return "/boardProducts/qnaUpdateForm";
	}

	@RequestMapping("updateQnA") 
	public String updateQnA(HttpServletRequest request,BoardQnADTO dto,int bq_no) throws Exception {	
		bq_no = Integer.parseInt(request.getParameter("bq_no"));

		String title = request.getParameter("bq_title");
		String checkedSecret = request.getParameter("checkedSecret");
		if(checkedSecret==null) {
			dto.setBq_checkedSecret("n");
		}else{
			dto.setBq_checkedSecret("y");
		}
		qnaService.updateQnA(dto, bq_no);

		return "redirect:/readQnA?mine=y&bq_no="+bq_no;
	}
	
	@ResponseBody
	@RequestMapping("updateImgs")
	public String updateImgs(HttpServletRequest request, BoardQnADTO dto, MultipartFile formData) {
		String id = (String) request.getAttribute("id");
		System.out.println(id);
		String path = "D:\\SpringOnly\\finalProject\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\MyGarden\\resources\\";		
		File dir = new File(path + "temp/"); //폴더경로
		System.out.println("폴더 존재? : " + dir.isDirectory());
		if(!dir.isDirectory()) { // 폴더가 있는지 확인.
			System.out.println("폴더생성");
			dir.mkdirs(); // 없으면 생성
		}
		

		String resourcePath = session.getServletContext().getRealPath("/resources/temp");
		String renamedFilePath = resourcePath + "/" + System.currentTimeMillis() + "_temp_profileImage.png";
		String result = null;

		try {
			File newFile = new File(resourcePath+"/"+System.currentTimeMillis()+"_temp_profileImage.png");
			formData.transferTo(newFile);
			String filePath = "/resources/temp/" + newFile.getName();
			result = newFile.getName();
			//System.out.println(filePath);
//			dto.setProfileImage(newFile.getName());

			//System.out.println(result);

		} catch (IOException e) {
			e.printStackTrace();
			return "error";
		}
		return result;
	}
	

	@ResponseBody
	@RequestMapping("writeComment")
	public Map<String, String> writeAnswer(HttpServletRequest request, CommentQnADTO dto) throws Exception{
		int cq_p_no = Integer.parseInt(request.getParameter("cq_p_no"));
		int cq_no = Integer.parseInt(request.getParameter("cq_no"));
		String cq_name = request.getParameter("cq_name");
		String cq_email = request.getParameter("cq_email");
		String cq_comment = request.getParameter("cq_comment");
		qnaService.writeComment(cq_no, dto);
		request.setAttribute("checkAns", 'y');
		Map <String,String> result = new HashMap<>();
		result.put("cq_comment", cq_comment);
		result.put("checkAns", "y");
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "updateComment", produces = "application/text; charset=utf8")
	public String updateComment(HttpServletRequest request, int cq_no,String cq_comment) throws Exception {
		qnaService.updateComment(cq_no, cq_comment);		
		return cq_comment;
	}

	@ResponseBody
	@RequestMapping("deleteComment")
	public int deleteComment(HttpServletRequest request, int cq_no) throws Exception {
		int result = qnaService.deleteComment(cq_no);	
		System.out.println(cq_no);
		return result; //result=2라면, 성공
	}



}
