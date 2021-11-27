			<div class="leftside">
				<ul class="section-title no-margin-top">
					<li><h3 style="font-family: Arial,sans-sherif!important;">Les dernières nouveautés</h3></li>
				</ul>
			
				
				<div style="width:100%!important;" class="col-md-9 col-xs-12" >
					<div class="row">
						<!-- 12 Columns -->
						<div class="col-md-12">
							<div class="alert alert-warning no-border-radius">
								Classé du plus récent au plus vieux, restez informer !
							</div>
							<ul class="timeline">
								<?php
								$i = 0;
								
								$query = $connection -> prepare('SELECT COUNT(*) FROM `site.timeline.news`;');
								$query -> execute();
								$row = $query -> fetch();
								$query -> closeCursor();

								$moyenne = ceil($row['COUNT(*)'] / 10);

								if(isset($_GET['num']) && is_numeric($_GET['num']))
									$page = $_GET['num'];
								else
									$page = 1;
									
								$start = ($page - 1) * 10;
								$query = $connection -> query("SELECT * FROM `site.timeline.news` ORDER BY id DESC LIMIT $start, 10;");
								$query -> execute();
								$query -> setFetchMode(PDO:: FETCH_OBJ);
								
								while ($news = $query->fetch()) { ?>
									<li <?php if($i % 2) echo 'class="timeline-inverted"'; ?>>
										<div class="timeline-badge primary"></div>
										<div class="timeline-panel">
											<div class="timeline-heading">
												<h4 class="padding-15"><a href="#"><?php echo $news -> title; ?></a></h4>
												<?php 
												if(!empty($news -> img))
													echo '<img class="img-responsive full-width" src="' . $news -> img . '" alt="" />';
												?>
											</div>
											<div class="timeline-body">
												<p><?php echo $news -> content; ?></p>
											</div>
											<div class="timeline-footer">
												<i class="ion-android-calendar"></i> <?php echo convertDateToString($news -> date); ?>
												<a class="pull-right"><i class="ion-android-forums"></i>0</a></a>
											</div>
										</div>
									</li>
								<?php
									$i++;
								}
								?>
								
								<li class="clearfix" style="float: none;"></li>
							</ul>	
							
							<center>
								<div class="btn-group">
									<a href=<?php if($page - 1 > 0) echo "?num=" . ($page - 1); else echo "#"; ?> class="btn btn-sm btn-default"><i class="fa fa-chevron-left"></i></a>
									<div class="btn btn-sm btn-default"><?php echo $page . " / " . $moyenne; ?></div>
									<a href=<?php if($page + 1 <= $moyenne) echo "?num=" . ($page + 1); else echo "#"; ?> class="btn btn-sm btn-default"><i class="fa fa-chevron-right"></i></a>
								</div>	
							</center>
						</div>
					</div>
				</div>
				<!-- ./12 Columns -->			
			</div>
			<!-- ./leftside -->			