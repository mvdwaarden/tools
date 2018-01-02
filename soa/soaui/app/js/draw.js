function d3_do_draw(data) {
   d3.select(".d3content").select(".list").selectAll("li")
            .data(data.service.subway.line,function (d,i){
                return d.name;
            }).enter().append("li");
  d3.select(".d3content").select(".list").selectAll("li").style("font-weight", function (d) {
        if (d.status == "GOOD SERVICE") {
            return "normal";
        } else {
            return "bold";
        }
    }).text(function (d) {
        return d.name + ": " + d.status;
    });
} 

function d3_do_clear_svg(){
  childs = document.getElementsByClassName("d3_svg_result")[0].children;      
  if (childs && childs.length > 0){
    childs[0].parentNode.removeChild(childs[0]);  
  } 
}

function d3_do_draw_svg(svg){
    d3.xml(svg, "image/svg+xml", function(error, xml) {
        if (error) throw error;
        document.getElementsByClassName("d3_svg_result")[0].appendChild(xml.documentElement);
    });
}
