<font color="DBC309">유저 관리:</font>
<table height=25>
    <tr>
        <td><center><edit var="query" width=275 height=15></center></td>
    </tr>
</table>
<table>
    <tr>
        <td><button value="유저 검색" action="bypass -h admin_sg_find player $query" width=135 height=21 back="L2UI_CT1.Button_DF_Down" fore="L2UI_CT1.Button_DF"></td>
        <td><button value="HWID 검색" action="bypass -h admin_sg_find hwid $query" width=135 height=21 back="L2UI_CT1.Button_DF_Down" fore="L2UI_CT1.Button_DF"></td>
    </tr>
</table>
<br>
<br>
<br>
<br>

<font color="DBC309">동작:</font>
<table>
    <tr>
        <td><button value="가드컨피그 리로드" action="bypass -h admin_sg_reload config" width=140 height=21 back="L2UI_CT1.Button_DF_Down" fore="L2UI_CT1.Button_DF"></td>
    </tr>
</table>