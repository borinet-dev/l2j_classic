<html>
<body>
<title>관리자 메뉴</title>
<table width=265 bgcolor=CCCCCC>
    <tr>
        <td><button value="메인" action="bypass -h admin_admin" width=69 height=21 back="L2UI_CT1.Button_DF_Down" fore="L2UI_CT1.Button_DF"></td>
        <td><button value="관리" action="bypass -h admin_sg" width=69 height=21 back="L2UI_CT1.Button_DF_Down" fore="L2UI_CT1.Button_DF"></td>
        <td><button value="추방" action="bypass -h admin_sg_bans" width=69 height=21 back="L2UI_CT1.Button_DF_Down" fore="L2UI_CT1.Button_DF"></td>
	<td><button value="옵션" action="bypass -h admin_sg_modules" width=69 height=21 back="L2UI_CT1.Button_DF_Down" fore="L2UI_CT1.Button_DF"></td>
    </tr>
</table>
<br><br>
%content%<br><br>
<center>
   스마트가드 만료날짜: <font color="LEVEL">%expire_time%</font>
</center>
</body>
</html>